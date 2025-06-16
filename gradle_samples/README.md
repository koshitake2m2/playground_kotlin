# gradle_samples

## Tips

### docker

```bash
docker compose -f docker/gradle_samples/compose.yml up -d
```

### Run in Terminal

```bash
./gradlew :app:run
./gradlew :ktor:run
./gradlew :ktor:integrationTest --rerun-tasks --info
```

### Run in IDEA

1. Go to File which has a main func.
2. Run

### Hot Reload

```bash
# In terminal A
./gradlew -t :ktor:build
./gradlew -t :ktor:build -x test -i

# In terminal B
./gradlew -t :ktor:run
```

ref: https://ktor.io/docs/server-auto-reload.html
