![version](https://img.shields.io/badge/dynamic/xml?color=blue&label=Version&query=%2F%2A%5Blocal-name%28%29%3D'metadata'%5D%2F%2A%5Blocal-name%28%29%3D'versioning'%5D%2F%2A%5Blocal-name%28%29%3D'release'%5D&url=https%3A%2F%2Frepo.asheiou.cymru%2Freleases%2Fcymru%2Fasheiou%2Fconfigmanager%2Fmaven-metadata.xml)
[![GPL 3.0](https://img.shields.io/github/license/AeoliaXYZ/AshUtils?&logo=github&label=License)](LICENSE)
# ConfigManager

ConfigManager is a simple tool that allows for seamless
and automatic updating and repair of Bukkit config.ymls.

## Setup
To use this tool, you need to add the asheiou-repo to
your repositories and then add the package as a dependency.

### Repository

```xml
<!-- ... -->
<repositories>
  <repository>
    <id>asheiou-repo</id>
    <name>Asheiou Repo</name>
    <url>https://repo.asheiou.cymru/releases</url>
  </repository>
</repositories>
<!-- ... -->
```


### Package
```xml
<!-- ... -->
<dependencies>
  <dependency>
    <groupId>cymru.asheiou</groupId>
    <artifactId>configmanager</artifactId>
    <version>1.2</version>
    <scope>compile</scope>
  </dependency>
</dependencies>
<!-- ... -->
```

## Limitations
At present, this library only works on the default config.yml. If that's something 
you'd like to change, feel free to submit a PR!
