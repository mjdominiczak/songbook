## Google Play Console

### Główne informacje o aplikacji

#### 1. Nazwa aplikacji

Śpiewnik RRN

#### 2. Krótki opis

Śpiewnik RRN

#### 3. Pełny opis

Niniejszy śpiewnik w swojej pierwszej wersji jest elektroniczną wersją śpiewnika Ruchu Rodzin Nazaretańskich, który powstał w roku 2022 w diecezji radomskiej. Ma służyć jako pomoc do przygotowania i wykonywania oprawy muzycznej liturgii Mszy Świętej oraz śpiewów przy innych okazjach.

## Notatki dot. modelu danych

Piosenka
-> może mieć kilka wersji
-> może należeć do jednego lub wielu śpiewników
-> w każdym śpiewniku może mieć różną pozycję (pozycja będzie wnioskowana z alfabetycznego ułożenia piosenek w śpiewniku)
-> w różnych śpiewnikach może mieć różne wersje

Wersja
-> piosenka o nowym id
-> powiązana z innymi przez listę `other_versions`

Śpiewnik
-> zbiór id piosenek