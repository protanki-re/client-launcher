# ProTanki Client Launcher

[![Discord](https://img.shields.io/discord/1001791048651120692?label=Discord&style=flat-square)](https://discord.gg/Jk8TFZpeZE)

Launcher allows to start the ProTanki [official client](https://protanki-online.com/) with the [chainloader](https://github.com/protanki-re/client-chainloader) to connect to custom game and resource server.

## Usage

[Video tutorial](https://youtu.be/uwxIo3vDlmc) is available.

### Creating client profile

First, you need to create a client profile.
Client profile contains chainloader and application description (for Adobe AIR).

Application ID have to be unique for each started client.
Default is `ProTanki-${CRC32(root)}`

```bash
java -jar launcher.jar create ./my-profile

# Specify display name
java -jar launcher.jar create ./my-profile --name "My Profile"

# Specify custom application ID for Adobe AIR
java -jar launcher.jar create ./my-profile --id "ProTanki-12345"
```

### Editing client profile

You can set settings for the client profile.

```bash
java -jar launcher.jar edit ./my-profile \
  --runtime air \
  --runtime-executable "Path/to/Adobe/AIR/bin/adl" \
  --chainloader "Path/to/chainloader.swf" \
  --game-library "Path/to/library.swf"
  
# Set default game and resource servers
java -jar launcher.jar edit ./my-profile \
  --game-server "game.example.org:1337" \
  --resource-server "https://resources.example.org"
```

### Running client

```bash
java -jar launcher.jar run ./my-profile
```
 
You can also override client profile settings:

```bash
java -jar launcher.jar run ./my-profile \
  --game-server "game.example.org:1337" \
  --resource-server "https://resources.example.org"
```

## Runtimes

You can choose between [Adobe AIR](https://archive.org/details/adobe-air-sdk-archived-older-versions) and [Flash Player](https://archive.org/details/flash32-5y5r) to use for the client.

To use Adobe AIR, you need to specify the path to the `adl` executable in the `--runtime-executable` option.

To use Flash Player, you need to specify the path to the `flashplayer` executable in the `--runtime-executable` option.

```bash
# 'run' command can be used instead of 'edit'

java -jar launcher.jar edit ./my-profile \
  --runtime air \
  --runtime-executable "Path/to/Adobe/AIR/bin/adl"

java -jar launcher.jar edit ./my-profile \
  --runtime flashplayer \
  --runtime-executable "Path/to/Adobe/FlashPlayer/flashplayer"
```

## Download

Prebuilt binaries are available at [GitHub Releases](https://github.com/protanki-re/client-launcher/releases).

## Building

Build instructions available in [building.md](docs/building.md).
