## Subsonic
Version: 0.1.0

A compatibility plugin to smoothly connect Velocity and a GTNH server

Supported Velocity versions: 4.1.0+

### How to use

1. Install Velocity and GTNH
2. Set up Velocity config in `velocity.toml`
3. Install Subsonic jar to `plugins` folder where Velocity lives
4. Start Velocity to generate the config file
5. Open `config.yml` in the `plugins/subsonic` folder and set the `name` to the name of the GTNH server in Step 2
6. Restart Velocity

### Build

```shell
./gradlew build
```
