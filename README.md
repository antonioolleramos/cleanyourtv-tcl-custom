# CleanYourTV — TCL Custom Home

> Personalização do launcher open-source [CleanYourTV](https://github.com/Gathin23/cleanyourtv), baseada na versão **v3.1.0** e testada em uma **SEMP TCL 32S6500S (Android TV 9)**. Este repositório é um **fork com modificações**, não um launcher desenvolvido do zero.

## Visão geral

O objetivo foi simplificar a tela inicial de uma Smart TV com hardware limitado, preservando recursos como aplicativos de streaming, Chromecast, entradas HDMI, Wi-Fi e configurações da TV.

Na tela inicial, ficam visíveis apenas o papel de parede configurado pelo usuário, o relógio, a data, o clima e os atalhos superiores. A grade de aplicativos aparece ao pressionar **↓** no controle remoto e é ocultada com **Voltar** ou **↑** na primeira linha. Ao abrir a Home, nenhum botão superior fica destacado.

## O que foi personalizado

- **Tela inicial minimalista** com relógio e data centralizados.
- **Aplicativos ocultos por padrão**, com navegação por controle remoto e sem recriar a grade a cada abertura.
- **Foco inicial invisível**, para evitar destaque na engrenagem ou nos botões superiores.
- **Clima de Rio Grande (RS)** integrado ao topo da Home, com dados da API pública [Open-Meteo](https://open-meteo.com/), consulta limitada a uma vez por hora e tentativa posterior em caso de falha.
- **Preservação de entradas, Wi-Fi e configurações** do launcher original.
- **Sem animações novas ou serviços de clima em segundo plano**.

### Arquivos modificados

| Arquivo | Função |
| --- | --- |
| `launcher/app/src/main/java/com/gathin/tvhome/MainActivity.java` | Navegação e estados da tela inicial |
| `launcher/app/src/main/java/com/gathin/tvhome/TclHomeCustom.java` | Interface personalizada, relógio, clima e controle de visibilidade |
| `launcher/app/src/main/AndroidManifest.xml` | Permissão de rede necessária para consultar o clima |

O papel de parede é configurado separadamente por tema e **não é distribuído neste repositório**.

## Resultados observados

Medições realizadas na TV durante o processo de personalização e limpeza do sistema:

| Indicador | Registro inicial | Registro após otimização |
| --- | ---: | ---: |
| RAM disponível (`MemAvailable`) | ~712 MiB | ~867 MiB |
| Memória PSS do launcher | ~68 MiB (launcher original) | ~20 MiB (CleanYourTV) |
| Ocupação da partição de dados | 85% | 74% |
| Espaço livre em `/data` | ~630 MB | ~1,0 GB |

**Limitações da comparação:** as medições foram feitas em momentos diferentes, algumas após reiniciar a TV, e incluem a substituição do launcher **e** outras mudanças manuais via ADB. Não representam um benchmark controlado nem comprovam uma porcentagem específica de aumento de velocidade. A memória de swap logo após o reinício também não é comparável diretamente à de uma sessão longa.

> As desativações de pacotes do Android foram feitas manualmente na TV de testes e **não são aplicadas automaticamente** por este fork.

## Tecnologias

Java · Android TV · Android SDK · Gradle · ADB · PowerShell · JSON · API Open-Meteo

## Compilação

Pré-requisitos: **JDK 17**, **Android SDK Platform 34** e **Build Tools 34**. A partir da raiz do repositório, no Windows:

```powershell
cd launcher
.\gradlew.bat --no-daemon assembleRelease
```

O APK resultante fica em `launcher/app/build/outputs/apk/release/app-release.apk`.

Para instalar **somente na sua TV**, com ADB já autorizado:

```powershell
adb connect <IP_DA_SUA_TV>:5555
adb install -r .\app\build\outputs\apk\release\app-release.apk
```

Se houver erro de assinatura, **não desinstale automaticamente a versão existente**: faça backup, confira a assinatura e preserve os dados do aplicativo.

**Atenção:** a implementação do clima utiliza coordenadas aproximadas e fixas de Rio Grande, RS. Para outra cidade, altere a URL da API em `TclHomeCustom.java`. Os dados da previsão dependem de internet; o restante da Home continua funcionando se a consulta falhar.

## Autoria e créditos

- **Projeto original:** [Gathin23 / CleanYourTV](https://github.com/Gathin23/cleanyourtv), licença [MIT](LICENSE).
- **Esta adaptação:** personalização, compilação e testes em TV real, com assistência de inteligência artificial.
- **Fonte tipográfica original:** respeitar o arquivo `launcher/FONT-LICENSE-OFL.txt` incluído pelo projeto original.

Os arquivos de código do upstream permanecem sujeitos aos avisos de copyright e às licenças existentes. Este fork não é afiliado à TCL, à Google ou ao projeto oficial.

## Próximas melhorias possíveis

- Transformar cidade e frequência do clima em opções configuráveis.
- Adicionar testes automatizados de navegação por controle remoto.
- Fazer comparações de desempenho com condições de medição padronizadas.
