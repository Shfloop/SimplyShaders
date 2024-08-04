# Cosmic Quilt Example Mod
> The example mod for the [Cosmic Quilt](https://codeberg.org/CRModders/cosmic-quilt) Loader

## How to test/build
For testing in the dev env, you can use the `gradle run` task

For building, the usual `gradle build` task can be used. The output will be in the `build/libs/` folder

## Wiki
For a wiki on how to use Cosmic Quilt & Quilt, please look at the [Cosmic Quilt wiki](https://codeberg.org/CRModders/cosmic-quilt/wiki) 

## Notes
- Most project properties can be changed in the `gradle.properties`
- To change author, description and stuff that is not there, edit `src/main/resources/quilt.mod.json`
- The project name is defined in `settings.gradle`
- To add Quilt mods in the build, make sure to use `internal` rather than `implementation`