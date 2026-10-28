# Guia de otimização por ADB — SEMP TCL 32S6500S

> **Escopo:** registro do procedimento realizado na **SEMP TCL 32S6500S**, Android TV **9**, firmware **V343.009946** / variante **TCL-LA-RT41KB-S1**. Não é um pacote automático de limpeza nem uma lista universalmente segura para TVs TCL. **Faça backup** e selecione apenas o que não utiliza.

Este fork tem **duas partes distintas**: (1) alterações do launcher CleanYourTV em Java, contidas no código deste repositório; (2) **limpeza e ajustes manuais do Android via ADB**, feitos na TV de testes. **Compilar/instalar o launcher não executa o debloat.**

## 1. O que foi observado

O proprietário relatou que, após a otimização, **o sistema ficou muito mais leve, os aplicativos passaram a abrir mais rapidamente, a navegação ficou mais fluida e os travamentos do sistema original deixaram de ocorrer**. Essas mudanças foram percebidas no uso real, após combinar launcher personalizado, desativação de recursos dispensáveis e animações em `0.0`. As medições foram feitas **logo após ligar/reiniciar a TV em AMBOS os estados**: antes, com o launcher original, e depois, com a Home personalizada e as mudanças via ADB.

| Métrica | Original: logo após ligar | Otimizada: logo após ligar | Diferença |
| --- | ---: | ---: | ---: |
| RAM física total | 1.404 MiB | 1.404 MiB | Sem alteração |
| RAM disponível (`MemAvailable`) | ~712 MiB | ~867 MiB | **+155 MiB (+21,8%)** |
| PSS do launcher | ~68 MiB | ~20 MiB | **−48 MiB (−70,6%)** |
| Swap em uso | ~277 MiB | ~14,9 MiB | **−262 MiB (−94,6%)** |
| Ocupação de `/data` | 85% | 74% | **−11 pontos percentuais** |
| Livre em `/data` | ~630 MB | ~1.000 MB | **+370 MB** |

**Como interpretar:** os números comprovam que, nessas duas coletas, havia mais RAM disponível, menos swap usada, menor memória consumida pelo launcher e mais espaço livre. A comparação do mesmo momento relativo ao boot a torna mais útil do que confrontar uma TV recém-reiniciada com outra em uso prolongado. **Não** há, porém, um ensaio controlado com tempo exato pós-boot, mesmos apps abertos e repetidas amostras; não é possível atribuir todo o ganho a um único pacote, afirmar redução de X% no tempo de resposta ou garantir ausência de travamentos em qualquer outra TV.

## 2. Preparação

1. Anote modelo, firmware e opções atuais da TV. Garanta acesso ao launcher original ou um plano de restauração.
2. Instale o Android SDK **Platform Tools** no computador e ative as opções de desenvolvedor/depuração compatíveis com a sua TV.
3. Conecte computador e TV à mesma rede local. Autorize a conexão ADB **na própria TV** quando aparecer a solicitação.
4. Use o **IP real da sua TV** (não copie um IP fixo de outra instalação). Em PowerShell, com o `adb` no PATH:

```powershell
adb connect IP_DA_TV:5555
adb devices
adb shell getprop ro.build.version.release
adb shell getprop ro.build.display.id
adb shell pm list packages > pacotes-antes.txt
adb shell pm list packages -d > desativados-antes.txt
```

> Algumas firmwares exigem ADB por USB ou ativação própria de depuração pela rede. Não exponha a porta 5555 à internet. Ao terminar, desligue a depuração e faça `adb disconnect`.

## 3. Instale e teste o launcher alternativo PRIMEIRO

A personalização foi feita a partir do [CleanYourTV v3.1.0](https://github.com/Gathin23/cleanyourtv) e dos arquivos Java deste fork. Compile o projeto conforme o [README principal](../README.md#compilação), instale o APK e **confirme que Home, navegação pelo controle, Configurações, Entradas e Wi-Fi funcionam** antes de desativar o launcher do Google.

- Launcher substituto: `com.gathin.tvhome`.
- Launcher original: `com.google.android.tvlauncher`.
- O wallpaper da demonstração é pessoal e é configurado separadamente; o repositório não inclui o papel de parede como tema instalável.

## 4. Pacotes envolvidos na limpeza

A limpeza procurou eliminar a sobrecarga de **componentes não utilizados**: recomendações e widgets da Home antiga, serviços de personalização TCL, diagnósticos, assistente e voz, aplicativos legados e recursos opcionais. Menos componentes desnecessários podem reduzir atividades e consumo de recursos; na TV testada, isso integrou a otimização após a qual os apps passaram a abrir mais rápido e a navegação ficou mais fluida. **Não há medição isolada do impacto de cada pacote**.

A relação abaixo foi **consolidada a partir do histórico da sessão**. No trecho de terminal preservado, há confirmação explícita da resposta `new state: disabled-user` para **`com.google.android.katniss`**, **`com.google.android.speech.pumpkin`**, **`com.tcl.tvweishi`** e **`com.tcl.partnercustomizer`**. Os outros nomes aparecem no registro consolidado das alterações da mesma sessão, mas não dispomos neste repositório de uma captura integral do comando/retorno de cada um. Assim, a lista **não deve ser interpretada como auditoria atual do estado de todos os pacotes**.

| ID do pacote | Identificação / propósito | Atenção |
| --- | --- | --- |
| `com.google.android.tvlauncher` | Launcher original Google Android TV — Home padrão; só desativar depois de testar substituto | ⚠️ Pode afetar função importante |
| `com.tcl.appmarket2` | Loja de aplicativos TCL — Loja alternativa pré-instalada | Opcional conforme uso |
| `com.google.android.tvrecommendations` | Recomendações Android TV — Sugestões e conteúdo do launcher antigo | Opcional conforme uso |
| `com.tcl.bi` | Serviço TCL BI — Componente de métricas do fabricante | Opcional conforme uso |
| `com.google.android.feedback` | Feedback do Google — Envio de feedback/diagnósticos | Opcional conforme uso |
| `com.tcl.esticker` | TCL eSticker — Recurso complementar da interface TCL | Opcional conforme uso |
| `com.google.android.music` | Google Play Música — Aplicativo legado de música | Opcional conforme uso |
| `com.google.android.videos` | Google Play Filmes — Aplicativo legado de filmes | Opcional conforme uso |
| `com.google.android.katniss` | Assistente Google para TV — Atalhos e recursos do assistente de voz | ⚠️ Pode afetar função importante |
| `com.google.android.speech.pumpkin` | Reconhecimento de voz — Recursos de reconhecimento de fala | ⚠️ Pode afetar função importante |
| `com.tcl.partnercustomizer` | Personalização TCL — Personalizações do parceiro/fabricante | Opcional conforme uso |
| `com.tcl.tvweishi` | TV Weishi — Serviço de limpeza/otimização da TCL | Opcional conforme uso |
| `com.google.android.tv.bugreportsender` | Relatórios de erro Android TV — Envio de relatórios de falha | Opcional conforme uso |
| `com.tcl.partnercustomizer.resource` | Recursos da personalização TCL — Recursos/configuração do fabricante | Opcional conforme uso |
| `com.google.android.leanbacklauncher.partnerwidget` | Widget do Leanback — Widgets do launcher original | Opcional conforme uso |
| `com.google.android.leanbacklauncher.partnercustomizer` | Personalização do Leanback — Personalização do launcher original | Opcional conforme uso |
| `com.tcl.waterfall.overseas` | Recurso TCL Waterfall — Componente adicional da interface TCL | Opcional conforme uso |
| `com.google.android.play.games` | Google Play Games — Recursos de jogos Google | Opcional conforme uso |
| `com.android.printspooler` | Spooler de impressão — Impressão no Android | Opcional conforme uso |
| `com.google.android.tts` | Síntese de fala do Google — Voz sintetizada; pode afetar acessibilidade | ⚠️ Pode afetar função importante |
| `com.google.android.marvin.talkback` | TalkBack — Leitor de tela; essencial a quem usa acessibilidade | ⚠️ Pode afetar função importante |

### Como conferir o estado antes de alterar

```powershell
adb shell pm list packages -d
adb shell pm list packages -e
adb shell pm list packages | findstr /i "tcl google android"
```

**Desative individualmente**, somente um pacote existente e que você confirmou dispensável. Exemplo de um dos comandos realmente utilizados:

```powershell
adb shell pm disable-user --user 0 com.tcl.tvweishi
```

Para confirmar:

```powershell
adb shell pm list packages -d
```

**Como desfazer** a desativação de um pacote:

```powershell
adb shell pm enable --user 0 com.tcl.tvweishi
```

Se quiser retornar à Home antiga, **reative o launcher Google antes** de desabilitar ou remover o alternativo:

```powershell
adb shell pm enable --user 0 com.google.android.tvlauncher
```

**Não desative todos os 21 pacotes em lote.** Especialmente `com.google.android.marvin.talkback` (acessibilidade), `com.google.android.tts` (voz), `com.google.android.katniss` (assistente), `com.google.android.speech.pumpkin` (fala) e `com.google.android.tvlauncher` (Home original) exigem avaliação do uso e um caminho de recuperação. Os nomes variam entre versões e regiões do firmware. Um pacote ausente não é algo que você precise instalar ou desativar.

## 5. Animações do sistema

Também foram avaliadas as três escalas globais de animação:

```powershell
adb shell settings get global window_animation_scale
adb shell settings get global transition_animation_scale
adb shell settings get global animator_duration_scale
```

**Configuração final adotada e confirmada pelo proprietário: `0.0` nas três escalas (animações desativadas).** Os registros mostram que `0.5` também foi experimentado durante os ajustes, mas **não foi o valor mantido ao final**. Para reproduzir a configuração final da TV:

```powershell
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

# Confirme o estado:
adb shell settings get global window_animation_scale
adb shell settings get global transition_animation_scale
adb shell settings get global animator_duration_scale
```

Para **restaurar as animações padrão do Android** caso prefira:

```powershell
adb shell settings put global window_animation_scale 1
adb shell settings put global transition_animation_scale 1
adb shell settings put global animator_duration_scale 1
```

Desligar animações elimina o tempo das transições visuais e contribui para a sensação de maior agilidade, **mas não aumenta a frequência da CPU nem a RAM física**. A melhora de memória medida é consequência do conjunto de alterações, não desse ajuste isolado.

## 6. Serviços e funções preservados

O objetivo foi continuar usando os serviços essenciais: **Netflix, YouTube, Prime Video, Disney+, Max, Globoplay, Apple TV, Crunchyroll, Spotify, Chromecast/cast via iPhone, entradas HDMI, TV aberta, Wi-Fi, Bluetooth, configurações e controle remoto**. A combinação de aplicativos testados depende de quais estão instalados e ativos na sua TV. Não desative pacotes centrais de cast, `com.google.android.gms`, `com.android.systemui`, `com.tcl.tvinput` ou `com.tcl.settings` seguindo este guia.

## 7. Repetir a medição após reiniciar

Após cada etapa de mudanças, reinicie a TV, aguarde até terminar a inicialização e compare em condições **o mais parecidas possível** (mesmo tempo após boot, sem abrir aplicativos extras e com a mesma conexão de rede).

```powershell
adb shell cat /proc/meminfo
adb shell dumpsys meminfo com.gathin.tvhome
adb shell df -h /data
adb shell pm list packages -d
```

Compare especificamente `MemAvailable`, `SwapTotal - SwapFree`, **TOTAL PSS** do launcher e ocupação de `/data`. A memória disponível pode variar bastante durante tarefas normais. Para medir tempo de resposta e travamentos com números, faça testes repetidos de abertura e navegação; não deduza percentuais de velocidade apenas de métricas de RAM.

## 8. Recuperação e responsabilidade

- Mantenha um backup do APK original e dos arquivos modificados.
- Se algum aplicativo ou atalho deixar de funcionar, reative o pacote correspondente com `pm enable --user 0` e reinicie.
- Pacotes **desinstalados apenas para o usuário** podem exigir `cmd package install-existing --user 0 ID_DO_PACOTE`, se ainda estiverem presentes na imagem do firmware, ou reinstalação pela loja.
- Se perder a Home, tente reativar `com.google.android.tvlauncher` via ADB; não desative o launcher original antes de testar o novo.
- **Não é necessário root, overclock ou flash de firmware** para as etapas documentadas.
- Tudo nesta seção é **manual**; o fork não desabilita pacotes nem altera escala de animações por conta própria.

A documentação registra a experiência **deste aparelho**, não uma promessa de resultado idêntico para todas as TVs.
