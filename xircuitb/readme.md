![My Logo](logo.jpeg)

# 🚀 XircuitB – Circuit Breakers for Humans

Ever wanted to slap a **circuit breaker** on a method and magically make it resilient? 🪄 Well, your wish has been
granted.  
Just annotate with `@XircuitB` and voilà – resilience at zero effort.

Built on **Resilience4j**, XircuitB makes your methods **fail-safe, monitored, and surprisingly polite**.

---

## How It Works

Annotate a method, and XircuitB does the heavy lifting:

- Tracks failures and opens/closes the circuit automatically.
- Lets you provide a **fallback** for blocked calls.
- Lets you provide a **runtime configuration** group.
- Handles **multiple active periods** (time-of-day and day-of-week).
- Supports **multiple circuit breakers** on the same method.
- Automatically handle **sync and async** methods, just slap `@XircuitB` wherever you want!

---

## Usage Example

```java
import io.xircuitb.annotation.XircuitB;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@XircuitB
public String externalService(String input) {
    // your risky code here
    return callExternalApi(input);
}

// Fallback is automatic if provided
try{
    externalService("test");
} catch (CallNotPermittedException e){
  // handle manually if you didn’t provide a fallback
}
```

You can configure your circuit breakers in a few flexible ways:

1. **Annotation defaults** – set parameters directly on `@XircuitB` for quick inline configuration.
2. **Application YAML** – provide a default template in your `application.yml`, so you only override what you need in
   the annotation.
3. **Configuration providers** – implement `XircuitBConfigProvider` to generate a runtime configuration dynamically.
4. **Configuration templates** – through `XircuitBConfigRegistry`, you can register your custom configurations and retrieve them just by using their names

This means you can mix and match: have a default YAML template, override a few parameters inline, or even provide a full
runtime configuration programmatically. It’s **powerful and flexible**, without forcing you to repeat boilerplate
everywhere.

Hierarchy is : **YML Defaults** < **Config providers** < **Config templates** < **Inline annotation**. Every configuration is merged with your annotation params at runtime, to let you adjust your templates.

### YAML Circuit Breaker parameters

You can use the classic application format:

```yml
default-xirctuib:
  slidingWindowType: TIME_BASED
  minNumberOfCalls: 30
  ..........
```

Or just env variables

```yml
XB_SLIDING_WINDOW_TYPE: TIME_BASED
XB_MIN_NUM_CALLS: 30
  ..........
```

| YML Name                | Env Variable                | Default value      | Description                                                                                                                                                                                                                                                                           |
|-------------------------|-----------------------------|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| slidingWindowType       | XB_SLIDING_WINDOW_TYPE      | COUNT_BASED        | The type of sliding window used to calculate the failure rate. Possible values are:<br/>- COUNT_BASED: tracks the outcome of the last N calls (defined by CB_SLIDING_WINDOW_SIZE).<br/>- TIME_BASED: tracks all calls made in the last N seconds (defined by CB_SLIDING_WINDOW_SIZE). |
| slidingWindowSize       | XB_SLIDING_WINDOW_SIZE      | 100                | Size of the sliding window.<br/>- If COUNT_BASED: the number of recent calls considered.<br/>- If TIME_BASED: the duration (in seconds) of the time window.                                                                                                                           |
| minNumberOfCalls        | XB_MIN_NUM_CALLS            | 10                 | The minimum number of calls required before calculating the failure rate                                                                                                                                                                                                              |
| failureRateThreshold    | XB_FAILURE_RATE_THRESHOLD   | 75                 | The failure rate threshold (in percentage) that triggers the circuit breaker to open                                                                                                                                                                                                  |
| waitDurationInOpenState | XB_WAIT_DURATION_OPEN_STATE | 5000 (5s)          | The time that the circuit breaker stays open before transitioning to half-open                                                                                                                                                                                                        |
| numCallHalfOpen         | XB_NUM_CALL_HALF_OPEN       | 10                 | The number of permitted calls when the circuit breaker is half-open                                                                                                                                                                                                                   |
| activeFrom              | XB_ACTIVE_FROM              | 00:00              | The starting time of the period when the circuit breaker is active (format HH:mm)                                                                                                                                                                                                     |
| activeTo                | XB_ACTIVE_TO                | 23:59              | The ending time of the period when the circuit breaker is active (format HH:mm)                                                                                                                                                                                                       |
| activeDays              | XB_ACTIVE_DAYS              | DayOfWeek.values() | The days of the week when the circuit breaker is active. By default, all days are active                                                                                                                                                                                              |
| exceptionsToCatch       | XB_EXCEPTIONS_TO_CATCH      | Exception.class    | The type of exception that do count for circuit breaker metrics, it can be a custom exception class.                                                                                                                                                                                  |

## Advanced Features

Multiple Circuit Breakers on One Method

```java

@XircuitB(name = "breaker1")
@XircuitB(name = "breaker2")
public void riskyMethod() { ...}
```

Or use `@XircuitBs` for repeated annotations (but Sonar will complain about it).

## Active Periods

Control **when** a CB is applied:

```java
@XircuitB(
        activeFrom = "09:00",
        activeTo = "18:00",
        activeDays = {DayOfWeek.MONDAY, DayOfWeek.FRIDAY}
)
```
Use application yml for advanced scheduling configuration:
```yml
default-xircuitb:
   activePeriods:
     - from: "09:00"
       to: "18:00"
       days: [ MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY ]
     - from: "09:00"
       to: "13:00"
       days: [ SATURDAY ]
```

Outside the periods provided, the method runs **normally**, CB-free.

## Fallback Providers

Automatic fallback provider, you just need to pay attention if your method is sync or async to implement the correct
interface.

| Method Type | Fallback Interface              |
|-------------|---------------------------------|
| Sync        | `XircuitBFallbackProviderSync`  |
| Async       | `XircuitBFallbackProviderAsync` |

Pretty easy, right??

```java
import io.xircuitb.annotation.XircuitB;
import org.springframework.stereotype.Component;

@Component //please remember to annotate it as a spring bean
public class MySyncFallbackProvider implements XircuitBFallbackProviderSync {
    @Override
    public Object apply(CallNotPermittedException cause) {
        return "Fallback executed";
    }
}

@XircuitB(fallbackProvider = MySyncFallbackProvider.class)
public String doSyncStuff() {
}


@Component //please remember to annotate it as a spring bean
public class MyAsyncFallbackProvider implements XircuitBFallbackProviderAsync {
    @Override
    public CompletionStage<Object> apply(CallNotPermittedException cause) {
        return CompletableFuture.completedFuture("Async fallback executed");
    }
}

@XircuitB(fallbackProvider = MyAsyncFallbackProvider.class)
public CompletableFuture<String> doAsyncStuff() {
}

```

## Configuration Providers

Implement `XircuitBConfigProvider` for runtime configuration:

```java
import io.xircuitb.annotation.XircuitB;
import org.springframework.stereotype.Component;

@Component //please remember to annotate it as a spring bean
public class MyConfigurationProvider implements XircuitBConfigProvider {
    @Override
    public XircuitBConfigModel apply() {
        return yourCustomXircuitBConfigModel();
    }
}

@XircuitB(configProvider = MyConfigurationProvider.class)
```

## Global Registries

You can also register your templates through `XircuitBConfigRegistry` and `XircuitBFallbackRegistry`, so you can retrieve them just calling them by name.

```java
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.registry.XircuitBConfigRegistry;
import io.xircuitb.registry.XircuitBFallbackRegistry;

private final XircuitBConfigRegistry configRegistry;
private final XircuitBFallbackRegistry fallbackRegistry;

public void initConfigurationRegistry() {
   configRegistry.register("MyCustomConfiguration", XircuitBConfigModel.builder()......build()):
   fallbackRegistry.register("MyCustomFallback", XircuitBFallbackProvider.class);
}

@XircuitB(configTemplate = "MyCustomConfiguration", fallbackTemplate = "MyCustomFallback")
public void doStuff() {
    // your logic
}

```

## Monitoring & Logging

- Logs CB initialization and configuration at startup.
- Logs **state transitions** (CLOSED → OPEN → HALF_OPEN).
- Metrics available via `CircuitBreakerRegistry`.

## Testing & Integration

- Integration tests included: inactive CBs, multiple CBs, custom config/fallbacks.
- Easy to verify CB behavior without changing production code.

## Notes

- Default configs can be overridden via YAML or environment variables.
- Multiple CBs on a method? Use repeated annotations or `@XircuitBs`.
- If no fallback is provided, blocked calls throw `CallNotPermittedException`.

## License

This project is licensed under the Apache License – see the [LICENSE](LICENSE) file for details.
