# gradle_samples

## Tips

### Run in Terminal

```bash
./gradlew :app:run
./gradlew :ktor:run
```

### Run in IDEA

1. Go to File which has a main func.
2. Run

### Hot Reload

```bash
# In terminal A
./gradlew -t :ktor:build
# In terminal B
./gradlew -t :ktor:run
```
