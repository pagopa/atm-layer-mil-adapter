
# Coverage Report: JaCoCo

* mil-adapter
      
      
| Outcome                 | Value                                                               |
|-------------------------|---------------------------------------------------------------------|
| Code Coverage %         | 91.33%               |
| :heavy_check_mark: Number of Lines Covered | 474    |
| :x: Number of Lines Missed  | 45     |
| Total Number of Lines   | 519     |


## Details:

    
### it/gov/pagopa/miladapter/properties

<details>
    <summary>
:x: AdapterPoolConfigurationProperties.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: DefinitionIdProperties.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: RestConfigurationProperties.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: CacheConfigurationProperties.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: AuthProperties.java
    </summary>

        
</details>

    
### it/gov/pagopa/miladapter/model

<details>
    <summary>
:x: HTTPConfiguration.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: AuthParameters.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: Token.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: Configuration.java
    </summary>

        
</details>

    

<details>
    <summary>
:heavy_check_mark: ParentSpanContext.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: KeyToken.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:x: TerminalParameters.java
    </summary>

        
</details>

    
### it/gov/pagopa/miladapter

<details>
    <summary>
:x: MilAdapterApplication.java
    </summary>

        
#### Lines Missed:
        
- Line #14
```
    }
```
</details>

    
### it/gov/pagopa/miladapter/util

<details>
    <summary>
:heavy_check_mark: HttpRequestUtils.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:x: EngineVariablesToHTTPConfigurationUtils.java
    </summary>

        
#### Lines Missed:
        
- Line #27
```
    }
```
</details>

    

<details>
    <summary>
:heavy_check_mark: EngineVariablesUtils.java
    </summary>

        
#### All Lines Covered!
        
</details>

    
### it/gov/pagopa/miladapter/enums

<details>
    <summary>
:heavy_check_mark: RequiredProcessVariables.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: HttpVariablesEnum.java
    </summary>

        
#### All Lines Covered!
        
</details>

    
### it/gov/pagopa/miladapter/engine/task

<details>
    <summary>
:x: RestExternalTaskHandler.java
    </summary>

        
#### Lines Missed:
        
- Line #47
```
                        catch (Exception e) {
```
</details>

    
### it/gov/pagopa/miladapter/services

<details>
    <summary>
:x: MILRestService.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: TokenService.java
    </summary>

        
#### Lines Missed:
        
</details>

    

<details>
    <summary>
:x: GenericRestServiceNoAuth.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: IDPayRestService.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: CacheService.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: DefinitionIdRestService.java
    </summary>

        
</details>

    

<details>
    <summary>
:x: GenericRestService.java
    </summary>

        
#### Lines Missed:
        
- Line #55
```
                } catch (InterruptedException e) {
```
- Line #77
```
        } catch (HttpClientErrorException | HttpServerErrorException e) {
```
- Line #85
```
        }
```
- Line #140
```
            } catch (JsonProcessingException e) {
```
</details>

    
### it/gov/pagopa/miladapter/resttemplate

<details>
    <summary>
:heavy_check_mark: CustomHttpRequestRetryStrategy.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: RestTemplateGenerator.java
    </summary>

        
#### All Lines Covered!
        
</details>

    
### it/gov/pagopa/miladapter/engine/task/impl

<details>
    <summary>
:x: DefinitionIdRestTaskHandler.java
    </summary>

        
#### Lines Missed:
        
</details>

    

<details>
    <summary>
:heavy_check_mark: GenericHandler.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: IDPayRestTaskHandler.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:x: MILRestTaskHandler.java
    </summary>

        
#### Lines Missed:
        
</details>

    

<details>
    <summary>
:x: GenericRestNoAuthTaskHandler.java
    </summary>

        
#### Lines Missed:
        
</details>

    
### it/gov/pagopa/miladapter/services/impl

<details>
    <summary>
:x: MILRestServiceImpl.java
    </summary>

        
#### Lines Missed:
        
- Line #18
```
    }
```
</details>

    

<details>
    <summary>
:x: GenericRestServiceNoAuthImpl.java
    </summary>

        
#### Lines Missed:
        
- Line #35
```
    }
```
</details>

    

<details>
    <summary>
:heavy_check_mark: GenericRestServiceAbstract.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: CacheServiceImpl.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:x: DefinitionIdRestServiceImpl.java
    </summary>

        
#### Lines Missed:
        
- Line #23
```
    }
```
</details>

    

<details>
    <summary>
:heavy_check_mark: IDPayRestServiceImpl.java
    </summary>

        
#### All Lines Covered!
        
</details>

    
### it/gov/pagopa/miladapter/config

<details>
    <summary>
:heavy_check_mark: CacheConfiguration.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: RestThreadPoolConfig.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: CustomExpiryPolicy.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: CompletionThreadPoolConfig.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:x: HttpRequestInterceptor.java
    </summary>

        
#### Lines Missed:
        
- Line #73
```
        }
```
</details>

    

<details>
    <summary>
:heavy_check_mark: RestTemplateConfig.java
    </summary>

        
#### All Lines Covered!
        
</details>

    

<details>
    <summary>
:heavy_check_mark: OpenTelemetryConfig.java
    </summary>

        
#### All Lines Covered!
        
</details>

    
