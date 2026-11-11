# ZeroUI integration

The UI layer is being migrated from the upstream ZeroUI project.

- Upstream: https://github.com/Xocio/ZeroUI
- Imported upstream revision: `bbfba4258c0e698e540c320bdd59771fb9d3c767`
- License: Apache-2.0; see [ZeroUI-LICENSE](../licenses/ZeroUI-LICENSE).
- Imported source: `:kit` module, including the component system, theme system, navigation primitives, motion, and Liquid Glass surfaces.
- Current design direction: Miuix as the default UI family, ZeroUI theme and component APIs, Liquid Glass floating navigation.
- Android toolchain follows the upstream catalog at the imported revision: AGP 9.2.1, Kotlin 2.4.20, Gradle 9.4.1, Java 21, Android SDK 36 (stable compile/target API; SDK 37 package was unavailable in the CI image), Miuix 0.9.4.

## Integration boundary

The ZeroUI example application contains template-specific pages and data flows (device information, component gallery, update checker, and its own appearance repository). Those are not copied wholesale because they do not belong to the ZhouYi archive. The reusable UI Kit is imported directly. The archive now uses ZeroUI theme and scaffolding, collapsing blurred top bars, Liquid Glass card surfaces and bottom navigation, ZeroUI search fields and filter chips, and ZeroUI preference controls on the home, directory, settings, and full-screen reading pages. The archive's data model, sources, and text remain in the ZhouYi application.

## Build policy

Automatic push builds are temporarily disabled during the UI migration. The previous failed runs exposed two separate CI issues: the earlier runner used Gradle 8.13 with AGP 9.2.1, and a later workflow requested the unavailable Android 37 SDK package. The workflow now pins Gradle 9.4.1 and installs the stable Android 36 SDK/build-tools. Run the APK workflow manually only after the migration and source review are complete.
