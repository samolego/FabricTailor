# Fabric Tailor
[![GitHub license](https://img.shields.io/github/license/samolego/FabricTailor)](https://github.com/samolego/FabricTailor/blob/master/LICENSE)
[![build](https://github.com/samolego/FabricTailor/actions/workflows/building.yml/badge.svg)](https://github.com/samolego/FabricTailor/actions/workflows/building.yml)
![Serverside](https://img.shields.io/badge/Working-server--side%20only-blue)
![Singleplayer](https://img.shields.io/badge/Working-singleplayer-darkblue)
[![Curseforge downloads](http://cf.way2muchnoise.eu/full_fabrictailor_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/fabrictailor)
[![Discord](https://img.shields.io/discord/797713290545332235)](https://discord.gg/9PAesuHFnp)

A server-side or singleplayer skin changing / restoring mod.
Download it on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabrictailor)
or [Modrinth](https://modrinth.com/mod/FabricTailor)

## Who can see skins?
tl;dr:
* vanilla skins - all players
* hd + custom capes - those with mod

Please see the [wiki](https://github.com/samolego/FabricTailor/wiki) :wink:.

## Permissions

FabricTailor provides permission support for permission manager of your choice. Skin changing is granted by default.

* `/skin` command permission
```
fabrictailor.command.skin
```
* You can also disable skin clearing / setting only
```
fabrictailor.command.skin.set
```
* Or setting by URL only
```
fabrictailor.command.skin.set.url
```
* In general, each command subnode has its own permission node, lowercase and separated by dots.


## Setup

1. Clone the repository. Then run `./gradlew genSources`
2. Edit the code you want.
3. To build run the following command:

```
./gradlew build
```

## Thanks
This mod exists thanks to:
* [MineSkin API](https://mineskin.org) (Skin uploading & signing)
* [ely.by API](https://ely.by) (Skin values & signatures)

## License

This mod is licensed under `LGPLv3` license.
