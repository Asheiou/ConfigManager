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
    <url>https://repo.asheiou.uk/releases</url>
  </repository>
</repositories>
<!-- ... -->
```


### Package
```xml
<!-- ... -->
<dependencies>
  <dependency>
    <groupId>uk.asheiou</groupId>
    <artifactId>configmanager</artifactId>
    <version>1.0.1</version>
    <scope>compile</scope>
  </dependency>
</dependencies>
<!-- ... -->
```

## Limitations
At present, this package only works on config.yml files and cannot
handle nested YAML keys, i.e. in:
```yaml
key:
  nested-key: value
```
the package can only "see" and correct the top layer of keys. If
you'd like to help fix that, make a PR! I'd be happy to review it.