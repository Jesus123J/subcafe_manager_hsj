package com.subcafae.finantialtracker.util;

import java.util.Collections;
import java.util.List;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Scroll infinito para JTable: cuando el usuario se acerca al final del
 * scroll, pide la siguiente pagina en background (mostrando una fila
 * temporal "Cargando mas...") y agrega las filas al modelo.
 *
 * Uso: instalar() una vez por tabla y llamar activar(filasEnTabla,
 * offsetDatos) despues de cada carga inicial en modo "ultimos". Si otro
 * flujo (busqueda, filtro por fechas) rellena la tabla por su cuenta, el
 * helper lo detecta solo — el rowCount deja de coincidir con lo que el
 * mismo cargo — y se desactiva hasta la proxima activacion.
 *
 * Todo el estado se toca solo en el EDT (los AdjustmentEvent y los
 * invokeLater corren ahi); la pagina se carga en un hilo aparte.
 */
public final class ScrollInfinito {

    /** Carga una pagina y la devuelve ya mapeada a filas del modelo.
     *  IMPORTANTE: no debe saltarse filas — el offset de datos avanza
     *  con el tamano de lo devuelto. */
    public interface CargadorPagina {
        List<Object[]> cargar(int offset, int limite) throws Exception;
    }

    private static final int UMBRAL_PX = 100;

    private final JTable tabla;
    private final int tamanoPagina;
    private final CargadorPagina cargador;

    private volatile boolean cargando = false;
    private boolean activo = false;
    private boolean fin = false;
    private int filasEnTabla = 0;
    private int offsetDatos = 0;

    private ScrollInfinito(JTable tabla, int tamanoPagina, CargadorPagina cargador) {
        this.tabla = tabla;
        this.tamanoPagina = Math.max(tamanoPagina, 1);
        this.cargador = cargador;
    }

    public static ScrollInfinito instalar(JTable tabla, int tamanoPagina, CargadorPagina cargador) {
        ScrollInfinito si = new ScrollInfinito(tabla, tamanoPagina, cargador);
        JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tabla);
        if (scroll != null) {
            scroll.getVerticalScrollBar().addAdjustmentListener(e -> si.revisarScroll());
        }
        return si;
    }

    /**
     * Activar tras llenar la primera pagina en modo "ultimos".
     * filasEnTabla: filas que quedaron en el modelo. offsetDatos: registros
     * consumidos de la fuente (pueden diferir si algun mapeo fallo).
     */
    public void activar(int filasEnTabla, int offsetDatos) {
        this.filasEnTabla = filasEnTabla;
        this.offsetDatos = offsetDatos;
        this.fin = offsetDatos < tamanoPagina;
        this.activo = true;
    }

    public void desactivar() {
        activo = false;
    }

    private void revisarScroll() {
        if (!activo || cargando || fin) {
            return;
        }
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        // Otro flujo rellena/limpio la tabla: auto-desactivar.
        if (model.getRowCount() != filasEnTabla) {
            activo = false;
            return;
        }
        JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tabla);
        if (scroll == null) {
            return;
        }
        JScrollBar barra = scroll.getVerticalScrollBar();
        if (barra.getValue() + barra.getVisibleAmount() < barra.getMaximum() - UMBRAL_PX) {
            return;
        }

        cargando = true;
        // Fila temporal visible mientras llega la pagina.
        Object[] filaCargando = new Object[model.getColumnCount()];
        for (int i = 0; i < filaCargando.length; i++) {
            filaCargando[i] = "";
        }
        if (filaCargando.length > 0) {
            filaCargando[Math.min(1, filaCargando.length - 1)] = "Cargando mas...";
        }
        model.addRow(filaCargando);

        final int offset = offsetDatos;
        new Thread(() -> {
            List<Object[]> resultado;
            try {
                resultado = cargador.cargar(offset, tamanoPagina);
            } catch (Exception ex) {
                System.out.println("Scroll infinito: error cargando pagina -> " + ex.getMessage());
                resultado = Collections.emptyList();
            }
            final List<Object[]> nuevas = resultado;
            SwingUtilities.invokeLater(() -> {
                if (model.getRowCount() > 0) {
                    model.removeRow(model.getRowCount() - 1); // quitar "Cargando mas..."
                }
                for (Object[] fila : nuevas) {
                    model.addRow(fila);
                }
                filasEnTabla += nuevas.size();
                offsetDatos += nuevas.size();
                if (nuevas.size() < tamanoPagina) {
                    fin = true; // no hay mas datos (o fallo la pagina): detener
                }
                cargando = false;
            });
        }).start();
    }
}
