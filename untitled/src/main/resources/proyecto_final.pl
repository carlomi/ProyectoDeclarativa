% =====================================================================
%  Proyecto Final - Programación Declarativa - Ciclo 02/2026
%  Base de conocimiento (SWI-Prolog) para resolver integrales y
%  calcular el área bajo la curva entre dos límites a y b.

% =====================================================================
% INTEGRANTES:
% Javier Alejandro Caceres Campos - 00068223
% Rafael Gutierrez del Barrio - 00073123
% Guillermo Rafael Caceres Estrada - 00132623
% Carlos Roberto Rivas Orellana - 00001323

% =====================================================================
%  Uso directo desde consola:
%     ?- resolver(X, 3*X^2 + sin(X), 0, 1, I, Area).
%  Uso desde Java (la interfaz ejecuta):
%     swipl -q -g api -t halt proyecto_final.pl -- "3*x^2+sin(x)" 0 1
% =====================================================================

% ---------------------------------------------------------------------
% Constantes numéricas reconocidas
% ---------------------------------------------------------------------
cte(K) :- number(K), !.
cte(K) :- atom(K), memberchk(K, [pi, e]).

% ---------------------------------------------------------------------
% lineal(X, U, K): U es una función lineal de X (k*x + c) y K es su
% derivada. Permite aplicar sustitución u = k*x + c, du = k dx.
% ---------------------------------------------------------------------
lineal(X, U, 1)  :- U == X, !.
lineal(X, K*U, R) :- cte(K), lineal(X, U, R0), R is K*R0, !.
lineal(X, U*K, R) :- cte(K), lineal(X, U, R0), R is K*R0, !.
lineal(X, U/K, R) :- cte(K), lineal(X, U, R0), R is R0/K, !.
lineal(X, -U, R)  :- lineal(X, U, R0), R is -R0, !.
lineal(X, U+C, R) :- cte(C), lineal(X, U, R), !.
lineal(X, C+U, R) :- cte(C), lineal(X, U, R), !.
lineal(X, U-C, R) :- cte(C), lineal(X, U, R), !.
lineal(X, C-U, R) :- cte(C), lineal(X, U, R0), R is -R0, !.

% dividir(E, K, R): R = E/K, sin escribir "/1".
dividir(E, K, E) :- K =:= 1, !.
dividir(E, K, -E) :- K =:= -1, !.
dividir(E, K, E/K).

% ---------------------------------------------------------------------
% prim(F(u), u, P): tabla de integrales inmediatas  ∫ F(u) du = P
% ---------------------------------------------------------------------
prim(sin(U),   U, -cos(U)).                          % k) ∫ sin(x) dx
prim(cos(U),   U, sin(U)).                           % l) ∫ cos(x) dx
prim(tan(U),   U, -log(abs(cos(U)))).                % m) ∫ tan(x) dx
prim(asin(U),  U, U*asin(U) + sqrt(1 - U^2)).        % n) asin(x)
prim(acos(U),  U, U*acos(U) - sqrt(1 - U^2)).        % o) acos(x)
prim(atan(U),  U, U*atan(U) - log(1 + U^2)/2).       % p) atan(x)
prim(sinh(U),  U, cosh(U)).                          % q) sh(x)
prim(cosh(U),  U, sinh(U)).                          % r) cosh(x)
prim(tanh(U),  U, log(cosh(U))).                     % s) tanh(x)
prim(asinh(U), U, U*asinh(U) - sqrt(U^2 + 1)).       % t) arcsinh(x)
prim(acosh(U), U, U*acosh(U) - sqrt(U^2 - 1)).       % u) arccosh(x)
prim(atanh(U), U, U*atanh(U) + log(1 - U^2)/2).      % v) arctanh(x)
prim(exp(U),   U, exp(U)).                           % extra: e^u
prim(log(U),   U, U*log(U) - U).                     % extra: ln(u)

% ---------------------------------------------------------------------
% s(X, F, I): I es una antiderivada de F respecto de X.
% ---------------------------------------------------------------------

% a) ∫ k dx = k*x
s(X, K, K*X) :- cte(K), !.

% b) ∫ x dx = x^2/2
s(X, Y, X^2/2) :- Y == X, !.

% c) ∫ (k*x) dx = k*x^2/2
s(X, K*Y, K*X^2/2) :- cte(K), Y == X, !.
s(X, Y*K, K*X^2/2) :- cte(K), Y == X, !.

% d) ∫ x^n dx = x^(n+1)/(n+1);  caso n = -1: ln|x|
s(X, Y^N, log(abs(X))) :- Y == X, number(N), N =:= -1, !.
s(X, Y^N, X^N1/N1) :- Y == X, number(N), N1 is N + 1, !.
s(X, K/Y, K*log(abs(X))) :- cte(K), Y == X, !.

% i) ∫ a^x dx = a^x/ln(a)
s(X, A^Y, X) :- Y == X, number(A), A =:= 1, !.
s(X, e^Y, exp(X)) :- Y == X, !.
s(X, A^Y, A^X/log(A)) :- Y == X, number(A), A > 0, !.

% j) ∫ a^u dx, u = k*x + c  ->  a^u/(k*ln(a))
s(X, e^U, I) :- lineal(X, U, K), K =\= 0, dividir(exp(U), K, I), !.
s(X, A^U, A^U/(K*log(A))) :-
    number(A), A > 0, A =\= 1, lineal(X, U, K), K =\= 0, !.

% g) ∫ (u + v) dx = ∫ u dx + ∫ v dx
s(X, U + V, IU + IV) :- s(X, U, IU), s(X, V, IV), !.

% h) ∫ (u - v) dx = ∫ u dx - ∫ v dx
s(X, U - V, IU - IV) :- s(X, U, IU), s(X, V, IV), !.
s(X, -U, -IU) :- s(X, U, IU), !.

% f) ∫ (k*u) dx = k * ∫ u dx
s(X, K*U, K*IU) :- cte(K), s(X, U, IU), !.
s(X, U*K, K*IU) :- cte(K), s(X, U, IU), !.
s(X, U/K, IU/K) :- cte(K), K \== 0, s(X, U, IU), !.

% k) a v) funciones elementales con argumento u = k*x + c
s(X, F, I) :-
    compound(F), functor(F, _, 1),
    prim(F, U, P), lineal(X, U, K), K =\= 0,
    dividir(P, K, I), !.

% e) ∫ u dx: cualquier u que se pueda descomponer con las reglas
%    anteriores (suma, resta, constante por función, etc.).

% ---------------------------------------------------------------------
% Simplificación algebraica para mostrar resultados más limpios
% ---------------------------------------------------------------------
simp(E, E) :- (var(E) ; atomic(E)), !.
simp(E, R) :-
    E =.. [F|As], maplist(simp, As, Bs), E1 =.. [F|Bs],
    ( regla_simp(E1, E2) -> simp(E2, R) ; R = E1 ).

regla_simp(A+B, C) :- number(A), number(B), C is A+B.
regla_simp(A-B, C) :- number(A), number(B), C is A-B.
regla_simp(A*B, C) :- number(A), number(B), C is A*B.
regla_simp(A/B, C) :- integer(A), integer(B), B =\= 0, 0 is A mod B, C is A//B.
regla_simp(-A, C)  :- number(A), C is -A.
regla_simp(0+A, A).
regla_simp(A+0, A).
regla_simp(A-0, A).
regla_simp(1*A, A).
regla_simp(A*1, A).
regla_simp(A/1, A).
regla_simp(A^1, A).
regla_simp(-(-A), A).
regla_simp(A+(-B), A-B).
regla_simp(A-(-B), A+B).
regla_simp(K*(-A), -(K*A)) :- number(K).
regla_simp(K*(A/D), (K*A)/D) :- number(K), \+ number(A).
regla_simp((K*A)/D, (K1*A)) :- number(K), number(D), D =\= 0, 0 is K mod D, K1 is K//D.
regla_simp(-(A)/D, -(A/D)).
regla_simp(A/B/C, A/D) :- number(B), number(C), D is B*C.
regla_simp(A/N, -(A/M)) :- number(N), N < 0, M is -N.

sustituir(Old, New, T, New) :- T == Old, !.
sustituir(_, _, T, T) :- \+ compound(T), !.
sustituir(Old, New, T, R) :-
    T =.. [F|As], maplist(sustituir(Old, New), As, Bs), R =.. [F|Bs].

% ---------------------------------------------------------------------
% Evaluación numérica de una expresión en un punto
% ---------------------------------------------------------------------
evaluar(X, Expr, Valor, Res) :-
    copy_term(X-Expr, Valor-Expr2),
    cv(Expr2, Expr3),
    Res is Expr3.

% cv/2 reemplaza ^ por ** para que funcione con reales y negativos
cv(E, E) :- atomic(E), !.
cv(E, R) :-
    E =.. [F|As], maplist(cv, As, Bs),
    ( F == '^' -> G = '**' ; G = F ),
    R =.. [G|Bs].

% ---------------------------------------------------------------------
% Verificación de dominio en [A, B] (integrales impropias o fuera de
% dominio). Falla con un mensaje si la función no es continua.
% ---------------------------------------------------------------------
muestras(200).

punto(A, B, N, I, P) :- P is A + (B - A) * I / N.

problema_dominio(X, F, A, B, Msg) :-
    Lo is min(A, B), Hi is max(A, B),
    (   singular(X, F, Lo, Hi, Msg) -> true
    ;   muestras(N),
        between(0, N, I), punto(Lo, Hi, N, I, P),
        \+ catch((evaluar(X, F, P, V), V =:= V), _, fail)
    ->  format(atom(Msg), 'La función no está definida en x = ~4f (fuera de dominio)', [P])
    ).

% cero de U dentro del intervalo (1/u, u^-n, tan(u))
singular(X, F, Lo, Hi, Msg) :-
    sub_term(S, F), compound(S),
    (   S = U^N, number(N), N < 0, D = U
    ;   S = _/U, \+ cte(U), D = U
    ;   S = tan(U), D = cos(U)
    ),
    cambia_signo(X, D, Lo, Hi, P), !,
    format(atom(Msg), 'Integral impropia: la función diverge cerca de x = ~4f', [P]).

cambia_signo(X, D, Lo, Hi, P) :-
    muestras(N),
    between(0, N, I), punto(Lo, Hi, N, I, P),
    I1 is I + 1, I1 =< N, punto(Lo, Hi, N, I1, Q),
    catch((evaluar(X, D, P, V1), evaluar(X, D, Q, V2)), _, fail),
    ( V1 =:= 0 ; V1 * V2 < 0 ), !.

% ---------------------------------------------------------------------
% resolver(X, F, A, B, Integral, Area)
% ---------------------------------------------------------------------
resolver(X, Func, A, B, Integral, Area) :-
    s(X, Func, I0),
    % se simplifica sobre una copia con x atómica para no ligar X
    copy_term(X-I0, '$x'-Ic),
    simp(Ic, Is),
    sustituir('$x', X, Is, Integral),
    evaluar(X, Integral, B, ELs),
    evaluar(X, Integral, A, ELi),
    Area is ELs - ELi.

% ---------------------------------------------------------------------
% Normalización de lo que escribe el usuario
%   - la variable x (átomo) pasa a ser variable de Prolog
%   - alias: sh, ch, th, arcsin, arcsinh, ln, ...
% ---------------------------------------------------------------------
alias(sh, sinh).      alias(ch, cosh).      alias(th, tanh).
alias(senh, sinh).    alias(sen, sin).      alias(tg, tan).
alias(arcsin, asin).  alias(arccos, acos).  alias(arctan, atan).
alias(arcsinh, asinh). alias(arccosh, acosh). alias(arctanh, atanh).
alias(arcsh, asinh).  alias(arcch, acosh).  alias(arcth, atanh).
alias(ln, log).

normalizar(X, T, X) :- var(T), !.          % X mayúscula también vale
normalizar(_, T, T) :- number(T), !.
normalizar(X, x, X) :- !.
normalizar(_, T, T) :- atom(T), !.
normalizar(X, T, R) :-
    T =.. [F|As], maplist(normalizar(X), As, Bs),
    ( length(As, 1), alias(F, G) -> true ; G = F ),
    R =.. [G|Bs].

% ---------------------------------------------------------------------
% api/0: punto de entrada para la interfaz Java.
% Lee argv = [Funcion, A, B] e imprime líneas con el formato:
%   OK
%   INT<TAB>integral
%   AREA<TAB>valor
%   FA / FB<TAB>valor        (antiderivada evaluada en a y en b)
%   LIM<TAB>a b              (límites ya evaluados, ej. pi -> 3.14159...)
%   PTS<TAB>x:y x:y ...      (puntos para graficar, "nan" = hueco)
% o bien
%   ERR<TAB>mensaje
% ---------------------------------------------------------------------
api :-
    current_prolog_flag(argv, Argv),
    append(_, [FS, AS, BS], Argv), !,
    catch(api(FS, AS, BS), E, (mensaje_error(E, M), format("ERR\t~w~n", [M]))).
api :- format("ERR\tFaltan argumentos~n").

api(FS, AS, BS) :-
    leer_limite(AS, A, 'a'),
    leer_limite(BS, B, 'b'),
    leer_funcion(FS, X, F),
    (   s(X, F, _) -> true
    ;   throw(msg('No hay una regla para integrar esa función'))
    ),
    (   problema_dominio(X, F, A, B, Msg) -> throw(msg(Msg)) ; true ),
    resolver(X, F, A, B, I, Area),
    evaluar(X, I, A, FA), evaluar(X, I, B, FB),
    X = x,
    format("OK~n"),
    format("INT\t~w~n", [I]),
    format("AREA\t~15g~n", [Area]),
    format("FA\t~15g~n", [FA]),
    format("FB\t~15g~n", [FB]),
    format("LIM\t~w ~w~n", [A, B]),
    imprimir_puntos(FS, A, B).

leer_funcion(FS, X, F) :-
    replace_neg_exp(FS, FS2),
    catch(term_string(T, FS2), _, throw(msg('La función está mal escrita. Favor incluir signos entre variables.(Ej. 5*x)'))),
    normalizar(X, T, F).

% "x^-1" -> "x^ -1" (SWI necesita el espacio)
replace_neg_exp(S, R) :-
    split_string(S, "", " ", [S1]),
    atomic_list_concat(Parts, '^-', S1),
    atomic_list_concat(Parts, '^ -', R).

leer_limite(S, V, Nombre) :-
    (   catch((term_string(T, S), V0 is T, number(V0)), _, fail)
    ->  V is float(V0)
    ;   format(atom(M), 'El límite ~w no es un número válido', [Nombre]),
        throw(msg(M))
    ).

imprimir_puntos(FS, A, B) :-
    leer_funcion(FS, X, F),
    Lo is min(A, B), Hi is max(A, B),
    % si a = b se grafica una ventana de ancho 2 alrededor del punto
    ( Hi - Lo < 1.0e-9 -> M = 1.0 ; M is (Hi - Lo) * 0.15 ),
    PLo is Lo - M, PHi is Hi + M,
    N = 400,
    findall(P-V,
            ( between(0, N, I), punto(PLo, PHi, N, I, P),
              (   catch((evaluar(X, F, P, V0), V0 =:= V0), _, fail)
              ->  V = V0 ; V = nan ) ),
            Pts),
    format("PTS\t"),
    forall(member(P-V, Pts), format("~w:~w ", [P, V])),
    nl.

mensaje_error(msg(M), M) :- !.
mensaje_error(E, M) :- format(atom(M), 'Error: ~w', [E]).
