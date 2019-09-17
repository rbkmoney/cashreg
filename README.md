# cashreg


[![Build Status](http://ci.rbkmoney.com/buildStatus/icon?job=rbkmoney_private/cashreg/master)](http://ci.rbkmoney.com/job/rbkmoney_private/job/cashreg/job/master/)


Приложение для взаимодействия с поставщиками, которые отправляют данные в ККТ (Контрольно Кассовый Аппарат)

### Разработчики

- [Anatoly Cherkasov](https://github.com/avcherkasov)


### Содержание:

1. [Настройки](docs/settings.md)
1. [Инструкция](docs/manual/)


Отправка запросов на сервис:

```
http(s)://{host}:8022/cashreg/management - создание чека, запрос чека и запрос событий
http(s)://{host}:8022/cashreg/repairer - создание события для починки
```


### Что умеет:

- Чтение событий из kafka
- Подготовка запросов и отправка данных
- Логирование изменений
- Возможна переотправка при смене статуса
- Поддерживается идемпотентность запросов
- Корректно работает с товарами в которых встречаются кавычки в названии
- Сохраняется номер нашего запроса в кассу
