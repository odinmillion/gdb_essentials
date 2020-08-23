# gdb_essentials

![Build](https://github.com/odinmillion/gdb_essentials/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/14841-gdb-essentials.svg)](https://plugins.jetbrains.com/plugin/14841-gdb-essentials)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/14841-gdb-essentials.svg)](https://plugins.jetbrains.com/plugin/14841-gdb-essentials)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [x] [Publish a plugin manually](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html) for the first time.
- [x] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->

This plugin will help you transfer breakpoints from the CLion to the gdb process. 
All instructions will be copied into the clipboard. Then you can paste them into the standalone gdb debugger.

<br/><br/>

`1. Tools -> GDB Essentials -> Generate break from caret`

<br/><br/>

In that scenario you can set one breakpoint from the caret position.

<br/><br/>

```
break Reader::Run
```

<br/><br/>

```
break brown_belt/week3/pipeline.cpp:63
```

<br/><br/>

`2. Tools -> GDB Essentials -> Generate breaks from enabled breakpoints`

<br/><br/>

In that scenario you can set multiple breakpoints from the active breakpoints.

<br/><br/>

```
define set_breakpoints_713241
    break yellow_belt/other/starter.cpp:91
    break brown_belt/week3/pipeline.cpp:33
    break brown_belt/week3/pipeline.cpp:25
end
set_breakpoints_713241
```

<br/><br/>

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "GDB Essentials"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/odinmillion/gdb_essentials/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
