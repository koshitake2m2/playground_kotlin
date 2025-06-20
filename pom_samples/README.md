# pom_samples

## Usage

### Run

1. Open this project in the IDEA.
2. Open `com.example.springboot.SampleSprintbootApplication`
3. Run the main function.


### docker

```
docker compose -f docker/compose.yml build
docker compose -f docker/compose.yml up -d
docker compose -f docker/compose.yml restart
docker compose -f docker/compose.yml logs -f mysql8

mysql -h 127.0.0.1 -P3310 -u root -ppassword
```
