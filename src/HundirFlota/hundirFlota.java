package HundirFlota;

import java.util.Scanner;

/**
 * Juego de hundir la flota por consola.
 *
 * El programa genera un tablero para la CPU, coloca los barcos de forma aleatoria y permite al usuario
 * disparar indicando fila (letra) y columna (número) hasta agotar intentos o hundir todos los barcos.
 *
 * @author Ivan Ribes Moliner
 */
public class hundirFlota {
    static Scanner key = new Scanner(System.in);

    //region CODIGOS ANSI
    public static final String RESET = "\u001B[0m";
    public static final String NEGRITA = "\u001B[1m";
    public static final String CIAN = "\u001B[36m";
    public static final String AMARILLO = "\u001B[33m";
    public static final String ROJO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String NARANJA = "\u001B[38;5;208m";
    public static final String BLANCO = "\u001B[37m";
    //endregion

    /**
     * Punto de entrada del programa. Prepara la partida (dificultad, tamaño de tablero, barcos e intentos),
     * inicializa los tableros y ejecuta el bucle principal de juego hasta victoria o derrota.
     *
     * @param args
     */
    public static void main(String[] args) {

        System.out.printf("""
                %s%s====================================================%s
                %s%s        ⚓  H U N D I R   L A   F L O T A  ⚓%s
                %s%s                 I V A N   R I B É S%s
                %s%s====================================================%s
                
                %sPrepárate para la batalla naval...%s
                %s¡Que comience el combate!%s
                
                """,
                BLANCO, NEGRITA, RESET,
                BLANCO, NEGRITA, RESET,
                BLANCO, NEGRITA, RESET,
                BLANCO, NEGRITA, RESET,
                BLANCO, RESET,
                BLANCO, RESET
        );

        //region VARIABLES
        char[][] tableroCPU;
        char[][] tableroUser;

        //Sirve para comprobar tocado/hundido
        int[][] numeroBarco;
        int[] estadoBarcos;

        String[] barcos = {"lancha", "buque", "acorazado", "portaaviones"};
        char[] letraBarco = {'L', 'B', 'Z', 'P'};
        int[] longBarcos = {1, 3, 4, 5};
        int[] cantBarcos = new int[barcos.length];

        int dificultad;
        int intentos;
        boolean victoria = false;
        int fila;
        int columna;
        int minIntentos;

        int tamTablero = 10; //MAX 26
        //endregion

        //region PREPARAR PARTIDA
        dificultad = seleccionaDificultad();
        if (dificultad == 4) {
            do {
                System.out.print("Introduce el tamaño del tablero(Max 26): ");
                tamTablero = key.nextInt();
                if (tamTablero > 26) {
                    System.out.println("Tamaño maximo 26.");
                }
            } while (tamTablero > 26);
        }

        tableroCPU = new char[tamTablero][tamTablero];
        tableroUser = new char[tamTablero][tamTablero];
        numeroBarco = new int[tamTablero][tamTablero];
        crearTablero(tableroCPU);
        crearTablero(numeroBarco);
        crearTablero(tableroUser);


        if (dificultad == 4) {
            estadoBarcos = new int[colocarBarcosPersonalizado(tableroCPU, longBarcos, letraBarco,
                    numeroBarco, barcos, cantBarcos) + 1];
        } else {
            cantBarcos(dificultad, cantBarcos, barcos, tableroCPU, longBarcos);
            estadoBarcos = new int[recorrerBarcos(tableroCPU, cantBarcos, letraBarco,
                    longBarcos, numeroBarco, barcos) + 1];
        }

        minIntentos = contarMinIntentos(tableroCPU);
        intentos = cantIntentos(dificultad, tableroCPU,minIntentos);
        //endregion

        //region PARTIDA
        mostrarTablero(tableroUser);
        //mostrarTablero(numeroBarco);
        //mostrarTablero(tableroCPU);

        for (int i = intentos; i > 0 && !victoria; i--) {

            mostrarRondas(i, intentos);

            fila = seleccionarFila(tableroUser);
            columna = seleccionarColumna(tableroUser);
            System.out.println();

            comprobarJugada(tableroCPU, tableroUser, fila, columna, numeroBarco, estadoBarcos);
            mostrarTablero(tableroUser);

            victoria = comprobarVictoria(estadoBarcos);
        }

        if (victoria) {
            System.out.printf("%n%s%s✔ HAS GANADO!%s%n", VERDE, NEGRITA, RESET);
        } else {
            System.out.printf("%n%s%s✖ HAS PERDIDO%s%n", ROJO, NEGRITA, RESET);
            //mostrar el tablero con las posiciones por disparar en diferente color(si hay tiempo)
            mostrarTableroDerrota(tableroUser, tableroCPU);
        }

        System.out.printf("%s%sFin del juego%s%n", BLANCO, NEGRITA, RESET);
        //endregion
    }

    //region METODOS CREAR PARTIDA

    /**
     * Rellena un tablero de caracteres con '_' para inicializarlo como tablero vacío.
     *
     * @param tablero matriz que representa el tablero a inicializar
     */
    public static void crearTablero(char[][] tablero) {

        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                tablero[i][j] = '_';
            }
        }
    }

    /**
     * Rellena un tablero auxiliar numérico con -1.
     * Se usa para almacenar información auxiliar (identificación de barcos por casilla).
     *
     * @param tablero matriz auxiliar a inicializar
     */
    public static void crearTablero(int[][] tablero) {

        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                tablero[i][j] = -1;
            }
        }
    }

    /**
     * Genera un índice aleatorio válido dentro de los límites del tablero.
     * Se utiliza para obtener valores de fila o columna al colocar barcos.
     *
     * @param tableroCPU tablero de referencia para obtener el tamaño
     * @return índice aleatorio entre 0 y (tableroCPU.length - 1)
     */
    //llamar 2 veces, 1 fila, 1 columna
    public static int posRandom(char[][] tableroCPU) {
        return (int) (Math.random() * tableroCPU.length);
    }

    /**
     * Coloca los barcos de la CPU en el tablero, según la cantidad y longitud de cada tipo de barco.
     *
     * Para cada barco se selecciona una posición inicial aleatoria y una dirección válida (horizontal o vertical
     * según el tipo), asegurando que las posiciones estén libres. Además, se rellena la matriz auxiliar
     * {@code numeroBarco} con el identificador numérico del barco en cada casilla ocupada.
     *
     * @param tableroCPU  tablero de la CPU donde se colocan los barcos
     * @param cantBarcos  cantidad de barcos por tipo
     * @param letraBarco  letra que representa cada tipo de barco en el tablero
     * @param longBarco   longitud (tamaño) de cada tipo de barco
     * @param numeroBarco matriz auxiliar que guarda el número/ID de barco por casilla ocupada
     * @return identificador del último barco colocado
     */
    public static int recorrerBarcos(char[][] tableroCPU, int[] cantBarcos,
                                     char[] letraBarco, int[] longBarco, int[][] numeroBarco, String[] barcos) {

        int numBarco = 0;

        for (int i = 0; i < cantBarcos.length; i++) {
            numBarco = colocarBarco(tableroCPU, cantBarcos, i, letraBarco, longBarco, numeroBarco, barcos, numBarco);
        }
        return numBarco;
    }

    /**
     * Calcula una dirección válida para colocar un barco desde una posición inicial.
     *
     * Devuelve 1 si es posible colocar el barco en dirección positiva (derecha o abajo),
     * -1 si es posible colocarlo en dirección negativa (izquierda o arriba), o 0 si no es posible
     * en ninguna dirección desde esa posición.
     *
     * @param longBarco  longitud del barco a colocar
     * @param fila       posición inicial (fila)
     * @param columna    posición inicial (columna)
     * @param tableroCPU tablero donde se comprueba la disponibilidad de casillas
     * @return 1 si dirección positiva es válida, -1 si dirección negativa es válida, 0 si ninguna es válida
     */
    public static int direccionBarco(int longBarco, int fila, int columna, char[][] tableroCPU) {
        boolean dirPositiva = true;
        boolean dirNegativa = true;

        if (longBarco == 5) {

            for (int i = fila; i < (longBarco + fila) && i < tableroCPU.length; i++) {
                if (tableroCPU[i][columna] != '_') {
                    dirPositiva = false;
                } else if (fila + longBarco > tableroCPU.length) {
                    dirPositiva = false;
                }
            }

            for (int i = fila; i > (fila - longBarco) && i >= 0; i--) {
                if (tableroCPU[i][columna] != '_') {
                    dirNegativa = false;
                } else if (fila - longBarco < 0) {
                    dirNegativa = false;
                }
            }

        } else {

            for (int i = columna; i < (longBarco + columna) && i < tableroCPU.length; i++) {
                if (tableroCPU[fila][i] != '_') {
                    dirPositiva = false;
                } else if (columna + longBarco > tableroCPU[fila].length) {
                    dirPositiva = false;
                }
            }

            for (int i = columna; i > (columna - longBarco) && i >= 0; i--) {
                if (tableroCPU[fila][i] != '_') {
                    dirNegativa = false;
                } else if (columna - longBarco < 0) {
                    dirNegativa = false;
                }
            }
        }

        if (dirPositiva) {
            return 1;
        } else if (dirNegativa) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Calcula el número máximo aproximado de barcos que podrían colocarse para un tipo de barco concreto
     * en un tablero dado, teniendo en cuenta su longitud y las casillas actualmente libres ('_').
     *
     * <p>Se utiliza como apoyo en el modo PERSONALIZADO para limitar el valor máximo que puede introducir el usuario.</p>
     *
     * @param tablero tablero en el que se quiere estimar cuántos barcos caben
     * @param i índice del tipo de barco (relacionado con los arrays longBarcos, barcos, etc.)
     * @param longBarcos array con las longitudes de cada tipo de barco
     * @return número máximo estimado de barcos posibles para ese tipo
     */
    public static int cantBarcosPosible(char[][] tablero, int i, int[] longBarcos) {
        int cantBarcos = 0;
        boolean posValida = true;

        if (longBarcos[i] == 5) {

            if (tablero.length < 5) {
                cantBarcos = 0;
            } else {
                cantBarcos = (tablero.length * tablero.length) / longBarcos[i] / 2;
            }

        } else {
            for (int fila = 0; fila < tablero.length; fila++) {
                for (int columna = 0; columna <= tablero[fila].length - longBarcos[i]; columna++) {
                    posValida = true;
                    for (int j = 0; j < longBarcos[i]; j++) {
                        if (tablero[fila][columna + j] != '_') {
                            posValida = false;
                        }
                    }
                    if (posValida) {
                        cantBarcos++;
                    }
                }
            }
            if (longBarcos[i] == 1) {
                return cantBarcos / 3;
            } else {
                return cantBarcos / longBarcos[i];
            }
        }
        return cantBarcos;
    }

    /**
     * Permite al usuario definir cuántos barcos de cada tipo quiere colocar (modo PERSONALIZADO).
     *
     * <p>Para cada tipo de barco calcula el máximo posible en función del tablero y la longitud del barco,
     * solicita al usuario un valor válido, y finalmente coloca los barcos de forma aleatoria en el tablero.</p>
     *
     * @param tableroCPU tablero real de la CPU donde se colocan los barcos
     * @param longBarcos array con la longitud de cada tipo de barco
     * @param letraBarco array con la letra que representa cada tipo de barco en el tablero
     * @param numeroBarco matriz auxiliar donde se guarda el ID numérico de barco en cada casilla ocupada
     * @param barcos array con los nombres de los barcos (para mostrar mensajes al usuario)
     * @param cantBarcos array donde se guardará la cantidad elegida por el usuario para cada tipo de barco
     * @return ID del último barco colocado (sirve para dimensionar estructuras como estadoBarcos)
     */
    public static int colocarBarcosPersonalizado(char[][] tableroCPU, int[] longBarcos, char[] letraBarco,
                                                 int[][] numeroBarco, String[] barcos, int[] cantBarcos) {

        int maxBarcos;

        int numBarco = 0;

        for (int i = barcos.length - 1; i >= 0; i--) {
            if (tableroCPU.length < longBarcos[i]) {
                maxBarcos = 0;
            } else {
                maxBarcos = cantBarcosPosible(tableroCPU, i, longBarcos);
            }
            do {
                System.out.printf("Introduce el numero de %S (max %d):"
                        , barcos[i], maxBarcos);
                cantBarcos[i] = key.nextInt();
                if (cantBarcos[i] > maxBarcos || cantBarcos[i] < 0) {
                    System.out.println("Valor introducido no valido!");
                }

            } while (cantBarcos[i] > maxBarcos || cantBarcos[i] < 0);

            numBarco = colocarBarco(tableroCPU, cantBarcos, i, letraBarco, longBarcos, numeroBarco, barcos, numBarco);

        }

        return numBarco;
    }

    /**
     * Coloca en el tablero todos los barcos de un tipo concreto (según el índice {@code i}),
     * intentando posiciones aleatorias hasta encontrar una ubicación válida.
     *
     * <p>Cuando consigue colocar un barco, marca sus casillas con la letra correspondiente y
     * rellena {@code numeroBarco} con el identificador del barco para poder controlar tocado/hundido.</p>
     *
     * <p>Si tras muchos intentos no se puede colocar un barco, se contabiliza como "imposible" y se informa al final.</p>
     *
     * @param tableroCPU tablero real de la CPU donde se colocan los barcos
     * @param cantBarcos array con la cantidad de barcos por tipo
     * @param i índice del tipo de barco que se está colocando
     * @param letraBarco array con la letra que representa cada tipo de barco
     * @param longBarco array con las longitudes de cada tipo de barco
     * @param numeroBarco matriz auxiliar donde se guarda el ID numérico de barco en cada casilla ocupada
     * @param barcos array con los nombres de barcos (para mensajes)
     * @param numBarco contador/ID actual del siguiente barco a colocar
     * @return nuevo valor de {@code numBarco} tras colocar todos los barcos de ese tipo
     */
    public static int colocarBarco(char[][] tableroCPU, int[] cantBarcos, int i, char[] letraBarco,
                                   int[] longBarco, int[][] numeroBarco, String[] barcos, int numBarco) {
        int fila;
        int columna;
        int direccion;
        int intentos;
        int barcosImposibles = 0;
        boolean colocado;

        for (int j = 0; j < cantBarcos[i]; j++) {
            intentos = (tableroCPU.length * tableroCPU.length) * 5;
            colocado = false;
            do {
                fila = posRandom(tableroCPU);
                columna = posRandom(tableroCPU);

                direccion = direccionBarco(longBarco[i], fila, columna, tableroCPU);

                if (i == 0) {
                    if (tableroCPU[fila][columna] == '_') {
                        tableroCPU[fila][columna] = letraBarco[i];
                        numeroBarco[fila][columna] = numBarco;
                        colocado = true;
                    }
                } else if (i == 3) {
                    if (direccion == 1) {
                        for (int k = fila; k < (fila + longBarco[i]); k += direccion) {
                            tableroCPU[k][columna] = letraBarco[i];
                            numeroBarco[k][columna] = numBarco;
                        }
                        colocado = true;
                    } else if (direccion == -1) {
                        for (int k = fila; k > (fila - longBarco[i]); k += direccion) {
                            tableroCPU[k][columna] = letraBarco[i];
                            numeroBarco[k][columna] = numBarco;
                        }
                        colocado = true;
                    }
                } else {
                    if (direccion == 1) {
                        for (int k = columna; k < (columna + longBarco[i]); k += direccion) {
                            tableroCPU[fila][k] = letraBarco[i];
                            numeroBarco[fila][k] = numBarco;
                        }
                        colocado = true;
                    } else if (direccion == -1) {
                        for (int k = columna; k > (columna - longBarco[i]); k += direccion) {
                            tableroCPU[fila][k] = letraBarco[i];
                            numeroBarco[fila][k] = numBarco;
                        }
                        colocado = true;
                    }
                }
                intentos--;
            } while (!colocado && intentos > 0);

            if (colocado) {
                numBarco++;
            } else {
                barcosImposibles++;
            }
        }
        if (barcosImposibles != 0) {
            System.out.printf("""
                No se han podido colocar %d %S, generación aleatoria imposible.
                Total %S: %d%n
                """, barcosImposibles, barcos[i], barcos[i], cantBarcos[i] - barcosImposibles);
        }

        return numBarco;
    }
    //endregion

    //region METODOS JUGADA

    /**
     * Solicita al usuario una fila en formato letra (A, B, C...) y la convierte a índice numérico.
     * Repite la petición hasta que la fila sea válida para el tamaño del tablero.
     *
     * @param tablero tablero de referencia para validar el rango
     * @return índice de fila (0 a tablero.length - 1)
     */
    public static int seleccionarFila(char[][] tablero) {
        char filaChar;
        int fila;

        do {
            System.out.printf("%s%s» FILA:%s ", BLANCO, NEGRITA, RESET);
            filaChar = key.next().toUpperCase().charAt(0);
            fila = filaChar - 'A';
            if (fila < 0 || fila > tablero.length - 1) {
                System.out.printf("%s%s✖ FILA NO VALIDA!%s%n", ROJO, NEGRITA, RESET);
            }
        } while (fila < 0 || fila > tablero.length - 1);

        return fila;
    }

    /**
     * Solicita al usuario una columna en formato numérico y la valida según el tamaño del tablero.
     * Repite la petición hasta que la columna sea válida.
     *
     * @param tablero tablero de referencia para validar el rango
     * @return índice de columna (0 a tablero.length - 1)
     */
    public static int seleccionarColumna(char[][] tablero) {
        int columna;

        do {
            System.out.printf("%s%s» COLUMNA:%s ", BLANCO, NEGRITA, RESET);
            columna = key.nextInt();
            if (columna < 0 || columna > tablero.length - 1) {
                System.out.printf("%s%s✖ COLUMNA NO VALIDA!%s%n", ROJO, NEGRITA, RESET);
            }
        } while (columna < 0 || columna > tablero.length - 1);

        return columna;
    }

    /**
     * Procesa la jugada del usuario en una coordenada (fila, columna).
     *
     * - Si ya se ha disparado en esa casilla del tablero del usuario, vuelve a pedir coordenadas.
     * - Si en el tablero de la CPU hay agua ('_'), marca 'A' en el tablero del usuario.
     * - Si hay barco, marca 'X', actualiza la matriz auxiliar y calcula el estado del barco (tocado/hundido).
     *
     * @param tableroCPU  tablero real de la CPU (con barcos)
     * @param tableroUser tablero visible para el usuario (con disparos)
     * @param fila        fila seleccionada por el usuario
     * @param columna     columna seleccionada por el usuario
     * @param tableroAux  matriz auxiliar con el ID de barco por casilla
     * @param estadoBarco array que guarda el estado de cada barco (por ID)
     */
    public static void comprobarJugada(char[][] tableroCPU, char[][] tableroUser, int fila, int columna,
                                       int[][] tableroAux, int[] estadoBarco) {
        int numBarco;
        int longBarco;

        while (tableroUser[fila][columna] != '_') {
            System.out.printf("%s%s✖ Ya has disparado aqui!!%s%n", ROJO, NEGRITA, RESET);
            fila = seleccionarFila(tableroUser);
            columna = seleccionarColumna(tableroUser);
        }

        if (tableroCPU[fila][columna] == '_') {
            tableroUser[fila][columna] = 'A';
            System.out.printf("%s%sAGUA!%s%n", CIAN, NEGRITA, RESET);
        } else if (tableroCPU[fila][columna] != '_') {
            char inicial = tableroCPU[fila][columna];
            numBarco = tableroAux[fila][columna];
            longBarco = switch (inicial) {
                case 'L' -> 1;
                case 'B' -> 3;
                case 'Z' -> 4;
                case 'P' -> 5;
                default -> 0;
            };
            tableroUser[fila][columna] = 'X';
            tableroAux[fila][columna] = -1;

            estadoBarcos(tableroAux, estadoBarco, fila, columna, numBarco, longBarco);
        }
    }

    /**
     * Actualiza y muestra el estado de un barco (TOCADO/HUNDIDO) tras un impacto.
     *
     * Calcula cuántas partes del barco siguen presentes en {@code tablero}. Si no quedan partes,
     * marca el barco como hundido (2); si quedan, lo marca como tocado (1).
     *
     * @param tablero   matriz auxiliar donde se identifican las partes restantes del barco
     * @param estado    array de estados de barcos por ID (0: nada, 1: tocado, 2: hundido)
     * @param fila      fila del último disparo (para comprobar horizontal)
     * @param columna   columna del último disparo (para comprobar vertical)
     * @param numBarco  identificador del barco impactado
     * @param longBarco longitud del barco impactado
     */
    public static void estadoBarcos(int[][] tablero, int[] estado, int fila, int columna,
                                    int numBarco, int longBarco) {

        int partesBarco = 0;

        if (longBarco < 5) {
            for (int i = 0; i < tablero[fila].length; i++) {
                if (tablero[fila][i] == numBarco) {
                    partesBarco++;
                }
            }
        } else {
            for (int i = 0; i < tablero.length; i++) {
                if (tablero[i][columna] == numBarco) {
                    partesBarco++;
                }
            }
        }

        if (partesBarco == 0) {
            System.out.printf("%s%sHUNDIDO!%s%n", ROJO, NEGRITA, RESET);
            estado[numBarco] = 2;
        } else {
            System.out.printf("%s%sTOCADO!%s%n", NARANJA, NEGRITA, RESET);
            estado[numBarco] = 1;
        }
    }

    /**
     * Comprueba si el jugador ha ganado.
     *
     * Se considera victoria cuando todos los barcos están en estado hundido (2).
     *
     * @param estados array con el estado de cada barco
     * @return {@code true} si todos los barcos están hundidos, {@code false} en caso contrario
     */
    public static boolean comprobarVictoria(int[] estados) {
        boolean victoria = true;

        for (int i = 0; i < estados.length; i++) {
            if (estados[i] != 2) {
                victoria = false;
            }
        }

        return victoria;
    }

    /**
     * Cuenta cuántas casillas del tablero de la CPU están ocupadas por barcos.
     *
     * <p>Este valor se utiliza como mínimo teórico de intentos: si el usuario elige menos intentos
     * que casillas ocupadas, sería imposible hundirlo todo aunque no fallase nunca.</p>
     *
     * @param tableroCPU tablero real de la CPU (contiene '_' en agua y letras en barcos)
     * @return número de casillas ocupadas por barcos
     */
    public static int contarMinIntentos(char[][] tableroCPU) {
        int minIntentos = 0;

        for (int i = 0; i < tableroCPU.length; i++) {
            for (int j = 0; j < tableroCPU[i].length; j++) {
                if (tableroCPU[i][j] != '_') {
                    minIntentos++;
                }
            }
        }
        return minIntentos;
    }

    //endregion

    //region METODOS SALIDA DATOS

    /**
     * Muestra por consola el tablero del usuario con formato de filas (letras) y columnas (números),
     * aplicando colores ANSI según el contenido:
     * '_' y 'A' en cian, 'X' en rojo.
     *
     * @param tableroUser tablero del usuario a mostrar
     */
    public static void mostrarTablero(char[][] tableroUser) {

        System.out.printf("%n%s%23S%s%n%n", ROJO + NEGRITA, "[ TABLERO ]", RESET);

        for (int i = -1; i < tableroUser.length; i++) {
            if (i == -1) {
                System.out.printf("%3c", ' ');
            } else {
                System.out.printf("%s%s%3d%s", BLANCO, NEGRITA, i, RESET);
            }
        }
        System.out.println();

        for (int i = 0; i < tableroUser.length; i++) {
            System.out.printf("%s%s%3c%s", BLANCO, NEGRITA, (char) ('A' + i), RESET);
            for (int j = 0; j < tableroUser.length; j++) {
                if (tableroUser[i][j] == 'A' || tableroUser[i][j] == '_') {
                    System.out.printf("%s%3c%s", CIAN, tableroUser[i][j], RESET);
                } else if (tableroUser[i][j] == 'X') {
                    System.out.printf("%s%3c%s", ROJO, tableroUser[i][j], RESET);
                } else {
                    System.out.printf("%3c", tableroUser[i][j]);
                }
            }
            System.out.println();
        }
    }

    /**
     * Muestra el tablero final en caso de derrota.
     *
     * Sustituye en el tablero del usuario todas las casillas no acertadas por el contenido real del tablero de la CPU,
     * para revelar la posición de los barcos. Luego imprime el tablero con formato y colores ANSI.
     *
     * @param tableroUser tablero del usuario (se completa con información del tableroCPU)
     * @param tableroCPU  tablero real de la CPU con la ubicación de los barcos
     */
    public static void mostrarTableroDerrota(char[][] tableroUser, char[][] tableroCPU) {

        for (int i = 0; i < tableroUser.length; i++) {
            for (int j = 0; j < tableroUser[i].length; j++) {
                if (tableroUser[i][j] != 'X') {
                    tableroUser[i][j] = tableroCPU[i][j];
                }
            }
        }

        System.out.printf("%n%s%23S%s%n%n", ROJO + NEGRITA, "[ TABLERO ]", RESET);

        for (int i = -1; i < tableroUser.length; i++) {
            if (i == -1) {
                System.out.printf("%3c", ' ');
            } else {
                System.out.printf("%s%s%3d%s", BLANCO, NEGRITA, i, RESET);
            }
        }
        System.out.println();

        for (int i = 0; i < tableroUser.length; i++) {
            System.out.printf("%s%s%3c%s", BLANCO, NEGRITA, (char) ('A' + i), RESET);
            for (int j = 0; j < tableroUser.length; j++) {
                if (tableroUser[i][j] == 'A' || tableroUser[i][j] == '_') {
                    System.out.printf("%s%3c%s", CIAN, tableroUser[i][j], RESET);
                } else if (tableroUser[i][j] == 'X') {
                    System.out.printf("%s%3c%s", ROJO, tableroUser[i][j], RESET);
                } else {
                    System.out.printf("%s%3c%s", VERDE, tableroUser[i][j], RESET);
                }
            }
            System.out.println();
        }
    }

    //no hace falta en la ejecucion, solo en test
    /**
     * Muestra por consola un tablero auxiliar de enteros.
     * Se utiliza principalmente para depuración o pruebas.
     *
     * @param tablero matriz numérica a mostrar
     */
    public static void mostrarTablero(int[][] tablero) {

        System.out.println("[TABLERO]");

        for (int i = 0; i <= tablero.length; i++) {
            if (i == 0) {
                System.out.printf("%3c", ' ');
            } else {
                System.out.printf("%3d", i);
            }
        }

        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero.length; j++) {
                System.out.printf("%3d", tablero[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Muestra por consola el número de rondas restantes con un color que varía según el porcentaje
     * de intentos restantes (verde, amarillo, naranja o rojo).
     *
     * <p>Si queda una única ronda, muestra el aviso de "ULTIMA RONDA".</p>
     *
     * @param ronda rondas restantes en ese momento
     * @param intentos número total de intentos iniciales
     */
    public static void mostrarRondas(int ronda, int intentos) {
        double porcentaje = (double) ronda / intentos;

        System.out.println();
        if (ronda == 1) {
            System.out.printf("%sULTIMA RONDA!%s%n%n", ROJO + NEGRITA, RESET);
        } else if (porcentaje > 0.75) {
            System.out.printf("RONDAS RESTANTES: %s%d%s%n%n", VERDE + NEGRITA, ronda, RESET);
        } else if (porcentaje > 0.50) {
            System.out.printf("RONDAS RESTANTES: %s%d%s%n%n", AMARILLO + NEGRITA, ronda, RESET);
        } else if (porcentaje > 0.25) {
            System.out.printf("RONDAS RESTANTES: %s%d%s%n%n", NARANJA + NEGRITA, ronda, RESET);
        } else {
            System.out.printf("RONDAS RESTANTES: %s%d%s%n%n", ROJO + NEGRITA, ronda, RESET);
        }
    }
    //endregion

    //region METODOS SELECCIONAR PARAMETROS

    /**
     * Muestra un menú de dificultad y solicita una opción válida al usuario.
     * Las opciones disponibles son: FACIL, MEDIO, DIFICIL y PERSONALIZADO.
     *
     * @return dificultad elegida (1..4)
     */
    public static int seleccionaDificultad() {
        int dificultad;

        System.out.printf("""
                %s%s[Selecciona una dificultad]%s
                
                %s%s[1] FACIL%s → 5 Lanchas - 3 Buques - 1 Acorazado - 1 Portaaviones
                        50 intentos.
                
                %s%s[2] MEDIO%s → 2 Lanchas - 1 Buque - 1 Acorazado - 1 Portaaviones
                        30 intentos.
                
                %s%s[3] DIFICIL%s → 1 Lancha - 1 Buque
                        10 intentos.
                
                %s%s[4] PERSONALIZADO%s → El usuario introduce los parametros.
                """,
                BLANCO, NEGRITA, RESET,
                VERDE, NEGRITA, RESET,
                NARANJA, NEGRITA, RESET,
                ROJO, NEGRITA, RESET,
                BLANCO, NEGRITA, RESET
        );

        do {
            System.out.printf("%s%s» Introduce la dificultad (1-4):%s ", BLANCO, NEGRITA, RESET);
            dificultad = key.nextInt();
            if (dificultad < 1 || dificultad > 4) {
                System.out.printf("%s%s✖ [ERROR] Dificultad no valida!%s%n", ROJO, NEGRITA, RESET);
            }
        } while (dificultad < 1 || dificultad > 4);

        return dificultad;
    }

    /**
     * Establece la cantidad de barcos por tipo según la dificultad elegida.
     *
     * @param dificultad dificultad seleccionada
     * @param cantBarcos array donde se guarda la cantidad de barcos por tipo
     * @param nombreBarco nombres de cada tipo de barco (no se usa aquí)
     */
    public static void cantBarcos(int dificultad, int[] cantBarcos, String[] nombreBarco,
                                  char[][] tablero, int[] longBarcos) {

        switch (dificultad) {
            case 1:
                cantBarcos = new int[]{
                        cantBarcos[0] = 5,
                        cantBarcos[1] = 3,
                        cantBarcos[2] = 1,
                        cantBarcos[3] = 1};
                break;
            case 2:
                cantBarcos = new int[]{
                        cantBarcos[0] = 2,
                        cantBarcos[1] = 1,
                        cantBarcos[2] = 1,
                        cantBarcos[3] = 1};
                break;
            case 3:
                cantBarcos = new int[]{
                        cantBarcos[0] = 1,
                        cantBarcos[1] = 1,
                        cantBarcos[2] = 0,
                        cantBarcos[3] = 0};
                break;
        }
    }

    /**
     * Devuelve el número de intentos según la dificultad.
     *
     * <p>En modo PERSONALIZADO (dificultad 4), el usuario introduce los intentos y se valida que:
     * <ul>
     *   <li>No supere el máximo posible ({@code tablero.length * tablero.length}).</li>
     *   <li>No sea inferior al mínimo necesario ({@code minIntentos}) para poder acertar todas las casillas ocupadas.</li>
     * </ul>
     *
     * @param dificultad dificultad seleccionada (1: fácil, 2: medio, 3: difícil, 4: personalizado)
     * @param tablero tablero de referencia para calcular el máximo de intentos posible
     * @param minIntentos mínimo de intentos recomendado/obligatorio para que sea posible tocar todas las casillas con barco
     * @return número de intentos para la partida
     */
    public static int cantIntentos(int dificultad, char[][] tablero, int minIntentos) {
        int maxIntentos = tablero.length * tablero.length;
        int intentos = 0;
        switch (dificultad) {
            case 1:
                intentos = 50;
                break;
            case 2:
                intentos = 30;
                break;
            case 3:
                intentos = 10;
                break;
            case 4:
                do {
                    System.out.print("Introduce el numero de intentos: ");
                    intentos = key.nextInt();
                    if (intentos > maxIntentos) {
                        System.out.println("El maximo de intentos es " + maxIntentos + "!");
                    } else if (intentos < minIntentos) {
                        System.out.println("El minimo de intentos es " + minIntentos + "!");
                    }
                } while (intentos > maxIntentos || minIntentos > intentos);
                break;
        }
        return intentos;
    }

    //endregion
}
