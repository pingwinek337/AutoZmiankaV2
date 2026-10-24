# AutoReroll

Klientowy mod Fabric (Minecraft 1.21.4), ktory automatycznie rolluje miecz zmiankami
i zatrzymuje sie, gdy procent wpadnie w ustawiony zakres (np. 47-50%).

## Wymagania
Minecraft 1.21.4, Fabric Loader 0.16.10+, Fabric API 0.119.3+1.21.4, Java 21.

## Uzycie
1. **O** - okno ustawien: suwaki zakresu, opoznienie, limit rolli.
2. **Wybierz** przy mieczu i zmiankach, potem klikasz je w swoim ekwipunku.
3. **Start** (albo **R** w ekwipunku). **R** zatrzymuje.

Mod zatrzymuje sie po trafieniu w zakres, po zamknieciu ekwipunku, przy limicie rolli
albo gdy skoncza sie zmianki.

## Config
`config/autoreroll.json`: `min`, `max`, `delayTicks`, `maxRolls`, `swordIndex`, `scrollIndex`,
`percentRegex` (regex z grupa 1 = liczba, domyslnie szuka liczby po slowie "obrazenia").

## Build
`gradle build` -> jar w `build/libs/`. GitHub Actions buduje go sam i wrzuca do Releases.

## Licencja
MIT
