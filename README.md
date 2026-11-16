# Integrales en Prolog — Proyecto Final Programación Declarativa 02/2026

- Base de conocimiento: `untitled/src/main/resources/proyecto_final.pl` (SWI-Prolog). Copia idéntica en `proyecto_final.pl`.
- Interfaz: Java 21 + Swing (`untitled/`) con aspecto de calculadora científica: pantalla LCD con gráfica, teclado numérico, tecla 2ND (funciones inversas), DEL, CLEAR, ◄ ►. Java ejecuta `swipl` como proceso y le pasa la función y los límites como argumentos.

## Requisitos
- SWI-Prolog instalado (`swipl` en el PATH, o variable de entorno `SWIPL` con la ruta al ejecutable).
  En Windows también se busca en `C:\Program Files\swipl\bin\swipl.exe`.
- JDK 21.

## Ejecutar
- IntelliJ: abrir `untitled/` y correr `uca.declarativa.Main`.
- Consola (sin IDE):
  ```
  cd untitled
  javac -d out $(find src -name '*.java') && cp src/main/resources/proyecto_final.pl out/
  java -cp out uca.declarativa.Main
  ```

## Probar la base de conocimiento sola
```
swipl proyecto_final.pl
?- resolver(X, 3*X^2 + sin(X), 0, 1, I, Area).
```
o como la usa Java:
```
swipl -q -g api -t halt proyecto_final.pl -- "3*x^2+sin(x)" 0 1
```

## Integrales soportadas
Las 22 del enunciado (a–v) + extras: `e^x`, `ln(x)`, `x/k`, `-u`, argumento lineal `f(k*x+c)`.
Detecta integrales impropias (ej. `1/x` en [-1, 1], `tan(x)` en [0, 2]) y funciones fuera de dominio (ej. `asin(x)` en [0, 2]).
