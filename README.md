# cashreg


[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/cashreg/master)](http://ci.rbkmoney.com/job/rbkmoney_private/job/cashreg/job/master/)


Приложение для взаимодействия с адаптерами взаимодействующими с провайдерами предоставляющих ККТ (Контрольно Кассовый Аппарат)

### Разработчики

- [Anatoly Cherkasov](https://github.com/avcherkasov)


### Содержание:

1. [Настройки](docs/settings.md)


Отправка запросов на сервис:

```
http(s)://{host}:8022/cashreg/management - создание чека, запрос чека и запрос событий
http(s)://{host}:8022/v1/processor - вызов обработчика
http(s)://{host}:8022/cashreg/repairer - создание события для починки
```
