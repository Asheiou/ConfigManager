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
