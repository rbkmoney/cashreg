# Настройки


#### Party Management

Данные для работы с Party Management

Параметры, которые можно переопределить при запуске приложения:

Название | Описание | Пример
------------ | ------------- | -------------
**service.partyManagement.url** | URL для взаимодействия | http://hellgate:8022/v1/processing/partymgmt
**service.partyManagement.networkTimeout** | Таймаут при взаимодействии с сервисом | 5000 (5 sec)


---


#### Dominant

Данные для работы с Dominant

Параметры, которые можно переопределить при запуске приложения:

Название | Описание | Пример
------------ | ------------- | -------------
**service.dominant.url** | URL для взаимодействия | http://dominant:8022/v1/domain/repository_client
**service.dominant.networkTimeout** | Таймаут при взаимодействии с сервисом | 5000 (5 sec)


---


#### Machinegun

Данные для работы с Dominant

Параметры, которые можно переопределить при запуске приложения:

Название | Описание | Пример
------------ | ------------- | -------------
**service.mg.networkTimeout** | Таймаут | 5000 (5 sec)
**service.mg.automaton.url** | URL | http://localhost:8080/v1/automaton
**service.mg.automaton.namespace** | Имя | cashreg


---


#### Остальные настройки

Название параметра | Описание | Пример
------------ | ------------- | -------------
**retry-policy.maxAttempts** | количество попыток | 3
**cache.maxSize** | максимальный размер кэша | 100
