my3DEngine
=========

Simple Java raycasting engine and level editor.

Run (Windows, PowerShell):

1. Compile

```powershell
javac -d bin src\*.java
```

2. Run the main game

```powershell
java -cp bin RaycastingEngine3D
```

3. Run the level editor

```powershell
java -cp bin EditorLauncher
```

Notes:
- The project has no external build tool. Using `javac`/`java` as above works for small experiments.
- If you use an IDE (IntelliJ/Eclipse/VS Code) import as a plain Java project.
- For faster rendering on very wide displays the renderer uses a horizontal scale factor; adjust `RENDER_SCALE` in `RaycastingEngine3D.java`.

Suggested next steps:
- Add a `build.gradle` or `pom.xml` for reproducible builds.
- Add JUnit tests and a small CI workflow.
- Extract configuration into a properties file for easier tuning.
 - Use the Gradle wrapper: `gradlew` / `gradlew.bat` (run `gradle wrapper` if `gradle-wrapper.jar` is missing).
