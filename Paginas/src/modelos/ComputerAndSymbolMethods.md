# Resumen de Métodos del Modelo

Este documento resume los métodos clave de las clases `Computer.java`, `Page.java` y `SymbolTable.java`.

---

## `Computer.java`

Esta clase simula el hardware de la computadora, principalmente la RAM y el Disco Duro (memoria virtual).

### Métodos Principales

* `Computer()` (Constructor)
    * Configura la simulación. Crea un `Disk` (memoria virtual) vacío y una `Ram` de 100 marcos (slots), llenando esos marcos con `null` para representar que están vacíos.

* `placePageInMemory(Page page, int index)`
    * Coloca una página en un marco (slot) específico de la RAM. Usa `Ram.set(index, page)`, lo que *reemplaza* lo que sea que esté en ese marco, sin cambiar el tamaño de la RAM.

* `sendPageToDisk(int index)`
    * Es el "swap-out" (intercambio). Toma la página del marco de RAM `index`, la agrega a la lista `Disk` (V-RAM), y luego pone `null` en el marco `index` de la RAM, dejándolo libre.

* `placePageInDisk(Page page)`
    * Simplemente agrega una página a la lista `Disk`. Es usado por `sendPageToDisk`.

### Métodos Ayudantes (Helpers)

* `findFreeFrameInRam()`
    * Busca en la `Ram` (de 0 a 99) y devuelve el *índice* del primer marco que esté `null` (vacío). Si no hay ninguno, devuelve `-1`.

* `isRamFull()`
    * Un atajo que te dice si `findFreeFrameInRam()` devolvió `-1`.

* `isPageInRam(Page page)`
    * Revisa si un objeto de página *específico* ya está en algún lugar de la RAM.

* `getRealMemoryUsed()` / `getVirtualMemoryUsed()`
    * Cuentan cuántas páginas hay en la `Ram` o en el `Disk` y lo multiplican por el tamaño de página (4KB) para darte el uso total en KB.

* `getTotalRamFragmentation()`
    * Suma el desperdicio (fragmentación interna) de todas las páginas que están *actualmente* en la RAM.

* `reset()`
    * Limpia la `Ram` y el `Disk` y vuelve a llenar la `Ram` con 100 marcos `null`, dejando todo como al principio.

### Getters / Setters

* Incluye los métodos estándar `getRam()`, `getDisk()`, `getPageKBSize()`, `setRam()`, etc., para acceder a las propiedades del computador.

---

## `Page.java`

Representa una única página de memoria. Es un objeto de datos simple.

* `Page(...)` (Constructor)
    * Crea el objeto página con su ID, índice, si está en V-RAM y un valor extra.

* `setFragmentationInBytes(int bytes)` / `getFragmentationInBytes()`
    * Permiten guardar y consultar cuántos bytes se están desperdiciando *dentro* de esta página. Esto solo importa para la última página de una instrucción `new` si el tamaño no era un múltiplo perfecto de 4096.

* `Getters / Setters Estándar`
    * `getId()`, `setId(int id)`
    * `getIndexOfPage()`, `setIndexOfPage(int index)` (El marco de RAM donde vive, o -1 si no está en RAM).
    * `isIsInVirtualMemory()`, `setIsInVirtualMemory(boolean val)` (La bandera que dice si está en RAM o Disco).
    * `getExtraValue()`, `setExtraValue(int val)` (El campo flexible para el bit 'R' de SC o el timestamp de LRU).

---

## `SymbolTable.java`

Es el "mapa de memoria" global de la MMU. Asocia los punteros (`ptr`) con las páginas que les pertenecen.

* `SymbolTable()` (Constructor)
    * Crea el `HashMap` interno (donde se guarda todo) y pone el contador `nextPointerId` en 1.

* `registerNewPointer(List<Page> pages)`
    * Se llama al ejecutar una instrucción `new`. Toma la lista de páginas recién creadas, les asigna un ID de puntero (`ptr`) nuevo y único, las guarda en el mapa y devuelve el `ptr`.

* `getPages(int ptr)`
    * Se llama al ejecutar `use` o `delete`. Le das un `ptr` (ej: 2) y te devuelve la `List<Page>` (la lista de páginas) asociada a él.

* `removePointer(int ptr)`
    * Se llama al ejecutar `delete` o `kill`. Borra el `ptr` del mapa y te devuelve la lista de páginas que *estaban* asociadas a él, para que la MMU sepa qué páginas debe liberar de la RAM/Disco.

* `pointerExists(int ptr)`
    * Una simple revisión (`true`/`false`) para ver si un `ptr` está actualmente en el mapa.

* `clear()`
    * Limpia el mapa por completo y resetea el contador de IDs a 1. Se usa para reiniciar la simulación.