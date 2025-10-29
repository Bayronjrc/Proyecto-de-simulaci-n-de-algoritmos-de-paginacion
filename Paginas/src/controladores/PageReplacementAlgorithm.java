package controladores;

import modelos.Page;
import java.util.List;

/**
 * Interfaz base para todos los algoritmos de paginación.
 * Define el contrato que deben cumplir OPT, FIFO, LRU, MRU, RND.
 */

public interface AlgoritmoPaginacion {
    /**
     * Selecciona la página victima a reemplazar cuando la RAM está llena.
     * 
     * @param pageInRAM Lista de pagina actualmente en RAM.
     * @return La pagina que dee ser enviada a disco, o null si no hay victimas
     */
    Page selectPageToReplace(List<Page> pageInRAM);

    /**
     * Actualiza los metadaos del algoritmo cuando ocurre un evento.
     * Eventos: "load"(pagina cargada), "use"(pagina usada), "evict"(pagina enviada a disco)
     * @param page La página involucrada en el evento.
     * @param event El tipo de evento que ocurrió.
     */

     void updateMetadata(Page page, String event);

     /**
      * Resetea el estado del algoritmo para una nueva simulación.
      */
      void reset();

      /**
       * Obtiene el nombre del algoritmo de paginación.
       * @return El nombre del algoritmo.
       */
      String getAlgorithmName();

      /**
       * Obtiene informacion detallada del estado actual del algoritmo,
       * útil para depuración o visualización.
       */
      String getDetailedState();

}
