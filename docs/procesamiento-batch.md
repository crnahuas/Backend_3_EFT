# Procesamiento batch de datos legacy

## Propósito

El servicio batch transforma los archivos operativos del sistema legacy en datos validados, trazables y fáciles de consultar. Los procesos se ejecutan de forma independiente y pueden reiniciarse cuando una falla interrumpe el trabajo.

## Fuente de datos

- Repositorio original: `https://github.com/KariVillagran/fin_legacy_data`
- Commit revisado: `1ca7c0432319a1fc30f9469078f0cabf7081579f`
- Archivos procesados: nueve CSV distribuidos entre `semana_1`, `semana_2` y `semana_3`.

La copia en `legacy-source` se mantiene intacta. Los mismos archivos se empaquetan como recursos del servicio para que las pruebas no dependan de una descarga externa.

## Jobs disponibles

1. `movimientosDiariosJob` valida fechas, montos y tipos de transacción, conserva anomalías y genera un resumen por fecha.
2. `interesesMensualesJob` valida cuenta, titular, saldo, edad y tipo; calcula el interés mensual y detecta registros duplicados. El archivo conserva el nombre original `intereses_trimestrales.csv` de la fuente legacy.
3. `estadosFinancierosAnualesJob` valida operaciones, clasifica depósitos y egresos, y genera un resumen por cuenta.

## Rendimiento y recuperación

- Cada archivo se procesa como una partición independiente.
- Un pool configurable ejecuta hasta cuatro particiones en paralelo.
- Los registros se escriben en lotes de 100 elementos.
- Las fallas transitorias de base de datos se reintentan hasta tres veces con una pausa configurable.
- Las filas CSV estructuralmente ilegibles pueden omitirse hasta el límite configurado.
- Una tarea programada detecta la última ejecución fallida y la reinicia automáticamente, con un máximo de dos intentos.
- Cada ejecución registra inicio, término, estado, steps y fallos en los logs.

## Integridad de datos

Una nueva ejecución limpia solamente las tablas que pertenecen a su job. Los datos con problemas de negocio se almacenan con estado `ANOMALIA` y una observación; no se descartan. La repetición de un job produce los mismos conteos y evita duplicar resultados.

## Resultados obtenidos

| Resultado | Total | Válidos | Anomalías |
|---|---:|---:|---:|
| Movimientos diarios | 1020 | 407 | 613 |
| Intereses mensuales | 1016 | 244 | 772 |
| Estados financieros anuales | 1018 | 295 | 723 |

También se generaron 339 resúmenes diarios y 20 resúmenes anuales por cuenta.
