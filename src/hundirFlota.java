import java.util.Scanner;

/**
 * Juego de hundir la flota por consola.
 *
 * El programa genera un tablero para la CPU, coloca los barcos de forma aleatoria y permite al usuario
 * disparar indicando fila (letra) y columna (número) hasta agotar intentos o hundir todos los barcos.
 *
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
    //endregion

    /**
     * Punto de entrada del programa. Prepara la partida (dificultad, tamaño de tablero, barcos e intentos),
     * inicializa los tableros y ejecuta el bucle principal de juego hasta victoria o derrota.
     *
     * @param args
     */
    public static void main(String[] args) {

        System.out.println("[HUNDIR LA FLOTA- IVAN RIBÉS]\n");

//region VARIABLES
        char[][] tableroCPU;
        char[][] tableroUser;

        //Sirve para comprobar tocado/hundido
        int[][] numeroBarco;
        int [] estadoBarcos;

        String[] barcos = {"lancha", "buque", "acorazado", "portaaviones"};
        char[] letraBarco = {'L', 'B', 'Z', 'P'};
        int[] longBarcos = {1, 3, 4, 5};
        int[] cantBarcos = new int[barcos.length];

        String dificultad;
        int intentos;
        boolean victoria = false;
        int fila;
        int columna;

        int tamTablero = 10; //MAX 25
//endregion

//region PREPARAR PARTIDA
        dificultad = seleccionaDificultad();
        if (dificultad.equals("PERSONALIZADO")) {
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

        cantBarcos(dificultad, cantBarcos, barcos);
        intentos = cantIntentos(dificultad, tableroCPU);

        crearTablero(tableroCPU);
        crearTablero(numeroBarco);
        estadoBarcos = new int[colocarBarcos(tableroCPU, cantBarcos, letraBarco, longBarcos, numeroBarco) +1];

        crearTablero(tableroUser);
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
            System.out.println("\nHas ganado!");
        } else {
            System.out.println("\nHas perdido");
            //mostrar el tablero con las posiciones por disparar en diferente color(si hay tiempo)
            mostrarTableroDerrota(tableroUser, tableroCPU);
        }

        System.out.println("Fin del juego");
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
     *
     * @param tableroCPU   tablero de la CPU donde se colocan los barcos
     * @param cantBarcos   cantidad de barcos por tipo
     * @param letraBarco   letra que representa cada tipo de barco en el tablero
     * @param longBarco    longitud (tamaño) de cada tipo de barco
     * @param numeroBarco  matriz auxiliar que guarda el número/ID de barco por casilla ocupada
     * @return identificador del último barco colocado
     */
    public static int colocarBarcos(char[][] tableroCPU, int[] cantBarcos,
                                    char[] letraBarco, int[] longBarco, int[][] numeroBarco) {
        int fila = 0;
        int columna = 0;
        int direccion = 0;
        int numBarco = -1;

        for (int i = 0; i < cantBarcos.length; i++) {
            for (int j = 0; j < cantBarcos[i]; j++) {
                do {
                    fila = posRandom(tableroCPU);
                    columna = posRandom(tableroCPU);

                    direccion = direccionBarco(longBarco[i], fila, columna, tableroCPU);

                    if (i == 0) {
                        if (tableroCPU[fila][columna] == '_') {
                            numBarco ++;
                            tableroCPU[fila][columna] = letraBarco[i];
                            numeroBarco[fila][columna] = numBarco;
                        }
                    } else if (i == 3) {
                        if (direccion == 1) {
                            numBarco ++;
                            for (int k = fila; k < (fila + longBarco[i]); k+=direccion) {
                                tableroCPU[k][columna] = letraBarco[i];
                                numeroBarco[k][columna] = numBarco;
                            }
                        } else if (direccion == -1){
                            numBarco ++;
                            for (int k = fila; k > (fila - longBarco[i]); k+=direccion) {
                                tableroCPU[k][columna] = letraBarco[i];
                                numeroBarco[k][columna] = numBarco;
                            }
                        }
                    } else {
                        if (direccion == 1) {
                            numBarco ++;
                            for (int k = columna; k < (columna + longBarco[i]); k+=direccion) {
                                tableroCPU[fila][k] = letraBarco[i];
                                numeroBarco[fila][k] = numBarco;
                            }
                        } else if (direccion == -1){
                            numBarco ++;
                            for (int k = columna; k > (columna - longBarco[i]); k+=direccion) {
                                tableroCPU[fila][k] = letraBarco[i];
                                numeroBarco[fila][k] = numBarco;

                            }
                        }
                    }

                } while (direccion == 0);
            }
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
     *
     * @param longBarco longitud del barco a colocar
     * @param fila posición inicial (fila)
     * @param columna posición inicial (columna)
     * @param tableroCPU tablero donde se comprueba la disponibilidad de casillas
     * @return 1 si dirección positiva es válida, -1 si dirección negativa es válida, 0 si ninguna es válida
     */
    public static int direccionBarco(int longBarco, int fila, int columna, char[][] tableroCPU) {
        boolean dirPositiva = true;
        boolean dirNegativa = true;

        if (longBarco == 5) {

            for (int i = fila; i < (longBarco+fila) && i < tableroCPU.length; i++) {
                if (tableroCPU[i][columna] != '_') {
                    dirPositiva = false;
                } else if (fila + longBarco >= tableroCPU.length) {
                    dirPositiva = false;
                }
            }

            for (int i = fila; i > (fila-longBarco) && i >= 0 ; i--) {
                if (tableroCPU[i][columna] != '_') {
                    dirNegativa = false;
                } else if (fila - longBarco <= 0) {
                    dirNegativa = false;
                }
            }

        } else {

            for (int i = columna; i < (longBarco+columna) && i < tableroCPU.length; i++) {
                if (tableroCPU[fila][i] != '_') {
                    dirPositiva = false;
                } else if (columna + longBarco >= tableroCPU[fila].length) {
                    dirPositiva = false;
                }
            }

            for (int i = columna; i > (columna-longBarco) && i >= 0 ; i--) {
                if (tableroCPU[fila][i] != '_') {
                    dirNegativa = false;
                } else if (columna - longBarco <= 0) {
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
            System.out.print("[FILA] -> ");
            filaChar = key.next().toUpperCase().charAt(0);
            fila = filaChar - 'A';
            if (fila < 0 || fila > tablero.length -1) {
                System.out.println("FILA NO VALIDA!");
            }
            //LETRA letra - a para sacar numero de fila
        } while (fila < 0 || fila > tablero.length);

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
            System.out.print("[COLUMNA] -> ");
            columna = key.nextInt();
            if (columna < 0 || columna > tablero.length -1) {
                System.out.println("COLUMNA NO VALIDA!");
            }
        } while (columna < 0 || columna > tablero.length);

        return columna;
    }

    /**
     * Procesa la jugada del usuario en una coordenada (fila, columna).
     *
     * - Si ya se ha disparado en esa casilla del tablero del usuario, vuelve a pedir coordenadas.
     * - Si en el tablero de la CPU hay agua ('_'), marca 'A' en el tablero del usuario.
     * - Si hay barco, marca 'X', actualiza la matriz auxiliar y calcula el estado del barco (tocado/hundido).
     *
     *
     * @param tableroCPU   tablero real de la CPU (con barcos)
     * @param tableroUser  tablero visible para el usuario (con disparos)
     * @param fila         fila seleccionada por el usuario
     * @param columna      columna seleccionada por el usuario
     * @param tableroAux   matriz auxiliar con el ID de barco por casilla
     * @param estadoBarco  array que guarda el estado de cada barco (por ID)
     */
    public static void comprobarJugada(char[][] tableroCPU, char[][] tableroUser, int fila, int columna,
                                       int[][] tableroAux, int[] estadoBarco) {
        int numBarco;
        int longBarco;

        while (tableroUser[fila][columna] != '_') {
            System.out.println("Ya has disparado aqui!!");
            fila = seleccionarFila(tableroUser);
            columna = seleccionarColumna(tableroUser);
        }

        if (tableroCPU[fila][columna] == '_') {
            tableroUser[fila][columna] = 'A';
            System.out.println("AGUA!");
        } else if (tableroCPU[fila][columna] != '_') {
            char inicial = tableroCPU[fila][columna];
            numBarco = tableroAux[fila][columna];
            longBarco = switch(inicial) {
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
     *
     * @param tablero   matriz auxiliar donde se identifican las partes restantes del barco
     * @param estado    array de estados de barcos por ID (0: nada, 1: tocado, 2: hundido)
     * @param fila      fila del último disparo (para comprobar horizontal)
     * @param columna   columna del último disparo (para comprobar vertical)
     * @param numBarco  identificador del barco impactado
     * @param longBarco longitud del barco impactado
     */
    public static void estadoBarcos (int[][] tablero, int[] estado, int fila, int columna,
                                     int numBarco, int longBarco){

        //No necesario para lancha, solo mirar que tableroCPU[fila][columna] == '_'
        //0 nada - 1 tocado - 2 hundido
        int partesBarco = 0;

        if (longBarco < 5) {
            //horizontal
            for (int i = 0; i < tablero[fila].length; i++) {
                if (tablero[fila][i] == numBarco) {
                    partesBarco++;
                }
            }
        } else {
            //vertical
            for (int i = 0; i < tablero.length; i++) {
                if (tablero[i][columna] == numBarco) {
                    partesBarco++;
                }
            }
        }

        if (partesBarco == 0) {
            System.out.println("HUNDIDO!");
            estado[numBarco] = 2;
        } else {
            System.out.println("TOCADO!");
            estado[numBarco] = 1;
        }
    }

    /**
     * Comprueba si el jugador ha ganado.
     *
     * Se considera victoria cuando todos los barcos están en estado hundido (2).
     *
     *
     * @param estados array con el estado de cada barco
     * @return {@code true} si todos los barcos están hundidos, {@code false} en caso contrario
     */
    public static boolean comprobarVictoria(int[] estados) {
        boolean victoria = true;

        for (int i = 0; i < estados.length; i++) {
            if (estados[i] != 2){
                victoria = false;
            }
        }

        return victoria;
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

        System.out.printf("%n%s%23S%s%n%n",ROJO + NEGRITA,"[ TABLERO ]",RESET);

        for (int i = -1; i < tableroUser.length; i++) {
            if (i == -1) {
                System.out.printf("%3c", ' ');
            } else {
                System.out.printf("%s%3d%s",AMARILLO ,i, RESET);
            }
        }
        System.out.println();

        for (int i = 0; i < tableroUser.length; i++) {
            System.out.printf("%s%3c%s",AMARILLO ,('A'+ i), RESET);
            for (int j = 0; j < tableroUser.length; j++) {
                if (tableroUser[i][j] == 'A' || tableroUser[i][j] == '_') {
                    System.out.printf("%s%3c%s",CIAN ,tableroUser[i][j], RESET);
                } else if (tableroUser[i][j] == 'X'){
                    System.out.printf("%s%3c%s",ROJO ,tableroUser[i][j], RESET);
                } else {
                    System.out.printf("%3c",tableroUser[i][j]);
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

        System.out.printf("%n%s%23S%s%n%n",ROJO + NEGRITA,"[ TABLERO ]",RESET);

        for (int i = -1; i < tableroUser.length; i++) {
            if (i == -1) {
                System.out.printf("%3c", ' ');
            } else {
                System.out.printf("%s%3d%s",AMARILLO ,i, RESET);
            }
        }
        System.out.println();

        for (int i = 0; i < tableroUser.length; i++) {
            System.out.printf("%s%3c%s",AMARILLO ,('A'+ i), RESET);
            for (int j = 0; j < tableroUser.length; j++) {
                if (tableroUser[i][j] == 'A' || tableroUser[i][j] == '_') {
                    System.out.printf("%s%3c%s",CIAN ,tableroUser[i][j], RESET);
                } else if (tableroUser[i][j] == 'X'){
                    System.out.printf("%s%3c%s",ROJO ,tableroUser[i][j], RESET);
                } else {
                    System.out.printf("%s%3c%s",VERDE,tableroUser[i][j], RESET);
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
     * de intentos restantes (verde, amarillo, naranja o rojo). Si queda una ronda, muestra "ULTIMA RONDA!".
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
     * @return dificultad elegida en mayúsculas
     */
    public static String seleccionaDificultad() {
        String dificultad;

        System.out.println("""
                [Selecciona una dificultad]
                
                FACIL → 5 Lanchas - 3 Buques - 1 Acorazado - 1 Portaaviones
                        50 intentos.
                
                MEDIO → 2 Lanchas - 1 Buques - 1 Acorazado - 1 Portaaviones
                        30 intentos.
                
                DIFICIL → 1 Lanchas - 1 Buques
                        10 intentos.
                
                PERSONALIZADO → El usuario introduce los parametros.""");

        do {

            System.out.print("Introduce la dificultad: ");
            dificultad = key.next().toUpperCase();
            if (!dificultad.equals("FACIL") && !dificultad.equals("MEDIO") &&
                    !dificultad.equals("DIFICIL") && !dificultad.equals("PERSONALIZADO")) {
                System.out.println("DIFICULTAD NO VALIDA!");
            }

        } while (!dificultad.equals("FACIL") && !dificultad.equals("MEDIO") &&
                !dificultad.equals("DIFICIL") && !dificultad.equals("PERSONALIZADO"));

        return dificultad;
    }

    /**
     * Establece la cantidad de barcos por tipo según la dificultad elegida.
     *
     * Para dificultad PERSONALIZADO, solicita al usuario el número de barcos de cada tipo.
     *
     *
     * @param dificultad dificultad seleccionada
     * @param cantBarcos array donde se guarda la cantidad de barcos por tipo
     * @param nombreBarco nombres de cada tipo de barco (para mostrar al usuario en PERSONALIZADO)
     */
    public static void cantBarcos(String dificultad, int[] cantBarcos, String[] nombreBarco) {

        switch (dificultad) {
            case "FACIL":
                cantBarcos = new int[]{
                        cantBarcos[0] = 5,
                        cantBarcos[1] = 3,
                        cantBarcos[2] = 1,
                        cantBarcos[3] = 1};
                break;
            case "MEDIO":
                cantBarcos = new int[]{
                        cantBarcos[0] = 2,
                        cantBarcos[1] = 1,
                        cantBarcos[2] = 1,
                        cantBarcos[3] = 1};
                break;
            case "DIFICIL":
                cantBarcos = new int[]{
                        cantBarcos[0] = 1,
                        cantBarcos[1] = 1,
                        cantBarcos[2] = 0,
                        cantBarcos[3] = 0};
                break;
            case "PERSONALIZADO":
                for (int i = 0; i < cantBarcos.length; i++) {
                    System.out.println("Introduce el numero de " + nombreBarco[i] + ": ");
                    cantBarcos[i] = key.nextInt();
                }
                break;
        }
    }

    /**
     * Devuelve el número de intentos según la dificultad.
     *
     * En PERSONALIZADO, solicita al usuario el número de intentos y valida que no supere el máximo
     * (tamaño del tablero al cuadrado).
     *
     *
     * @param dificultad dificultad seleccionada
     * @param tablero tablero de referencia para calcular el máximo de intentos posible
     * @return número de intentos para la partida
     */
    public static int cantIntentos(String dificultad, char[][] tablero) {
        int maxIntentos = tablero.length* tablero.length;
        int intentos = 0;
        switch (dificultad) {
            case "FACIL":
                intentos = 50;
                break;
            case "MEDIO":
                intentos = 30;
                break;
            case "DIFICIL":
                intentos = 10;
                break;
            case "PERSONALIZADO":
                do {
                    System.out.print("Introduce el numero de intentos: ");
                    intentos = key.nextInt();
                    if (intentos > maxIntentos){
                        System.out.println("El maximo de intentos es " + maxIntentos + "!");
                    }
                } while (intentos > maxIntentos);
                break;
        }
        return intentos;
    }
//endregion
}