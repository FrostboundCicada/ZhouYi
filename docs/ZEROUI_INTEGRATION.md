# ZeroUI integration

The UI layer is being migrated from the upstream ZeroUI project.

- Upstream: https://github.com/Xocio/ZeroUI
- Imported upstream revision: `bbfba4258c0e698e540c320bdd59771fb9d3c767`
- License: Apache-2.0; see [ZeroUI-LICENSE](../licenses/ZeroUI-LICENSE).
- Imported source: `:kit` module, including the component system, theme system, navigation primitives, motion, and Liquid Glass surfaces.
- Current design direction: Miuix as the default UI family, ZeroUI theme and component APIs, Liquid Glass floating navigation.
- Android toolchain follows the upstream catalog at the imported revision: AGP 9.2.1, Kotlin 2.4.20, Gradle 9.4.1, Java 21, Android SDK 37, Miuix 0.9.4.

## Integration boundary

The ZeroUI example application contains template-specific pages and data flows (device information, component gallery, update checker, and its own appearance repository). Those are not copied wholesale because they do not belong to the ZhouYi archive. The reusable UI Kit is imported directly; the archive's data model, reading pages, sources, and text remain in the ZhouYi application and are being adapted to the kit.

## Build policy

Automatic push builds are temporarily disabled during the UI migration. Run the APK workflow manually only after the migration and source review are complete.
