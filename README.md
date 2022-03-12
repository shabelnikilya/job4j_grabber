# job4j_grabber
--------
<b>Проект</b> - Агрегатор вакансий.
 - Выполняется парсинг страниц с вакансиями сайта [sql.ru](https://www.sql.ru/forum/job-offers) относящихся к Java.
Информация о вакансиях записывается в БД postgreSQL и также по REST API.
 - В проекте используются: Jsoup, Quartz, JDBC.
 - Скриншоты приложения:
<br>![image](images/1.jpg)
<br>![image](images/2.jpg)
 - Сборка и запуск проекта:
 Сборка проекта осуществляется с помощью Maven в jar файл. Команда запуска - java -jar target/Grabber.jar
 - Настройка подключения к БД, порт сервера для REST и врмемя обновления информации вынесено в app.properties. Их можно корректировать без изменения кода.
Сайт для парсинга также находится в этом файле и его можно изменить, но тогда возникнет необходимость реализовать интерфейс Parse.
 - Контакты: Telegram @ilya_hollow

[![Build Status](https://app.travis-ci.com/shabelnikilya/job4j_grabber.svg?branch=master)](https://app.travis-ci.com/shabelnikilya/job4j_grabber)
[![codecov](https://codecov.io/gh/shabelnikilya/job4j_grabber/branch/master/graph/badge.svg?token=71F5H5RT88)](https://codecov.io/gh/shabelnikilya/job4j_grabber)
