package com.example.aurora.moviesineedtowatch.tmdb;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Android Studio.
 * User: Iryna
 * Date: 25/01/18
 * Time:
 */

public class Const {
    public final static String DEBUG = "TMDBQM";
    public final static String TAG = "TAG";
    public final static String ERR = "VALUES";
    public final static String SEE = "LOOK HERE";

    public final static String TMDB_MOVIE = "http://api.themoviedb.org/3/movie/";
    public final static String TMDB_SEARCH = "http://api.themoviedb.org/3/search/movie";
    public final static String IMDb_MOVIE = "http://www.imdb.com/title/";
    public final static String IMAGE_PATH = "https://image.tmdb.org/t/p/";
    public final static String[] IMAGE_SIZE = {"w92", "w154", "w185", "w342", "w500", "w780", "original"}; //https://api.themoviedb.org/3/movie/1948/images?api_key=###
    public final static String EN = "?language=en-US&";
    public final static String RU = "?language=ru-RU&";
    public final static String Q = "?";
    public final static String QUERY = "&query=";

    public final static int ID = 0;
    public final static int ID_MOVIE = 1;
    public final static int ID_IMDB = 2;
    public final static int IMDB = 3;
    public final static int TITLE = 4;
    public final static int OTITLE = 5;
    public final static int OLANG = 6;
    public final static int OVERVIEW = 7;
    public final static int POST_PATH = 8;
    public final static int POST_IMAGE = 9;
    public final static int RELEASE_DATE = 10;
    public final static int TAGLINE = 11;
    public final static int RUNTIME = 12;
    public final static int VOTE_AVARG = 13;
    public final static int VOTE_COUNT = 14;
    public final static int GENRES_IDS = 15;
    public final static int COMPS = 16;
    public final static int COUNTRS = 17;
    public final static int LANG = 18;


    public final static HashMap<String, Locale> lang = new HashMap<String, Locale>() {
        {
            put("false", new Locale("ru"));
            put("true", Locale.ENGLISH);
        }

    };

    public final static HashMap<Integer, String[]> genres = new HashMap<Integer, String[]>() {
        {
            put(28,new String[]{"Action","Боевик"});
            put(12,new String[]{"Adventure","Приключения"});
            put(16,new String[]{"Animation","Мультфильм"});
            put(35,new String[]{"Comedy","Комедия"});
            put(80,new String[]{"Crime","Криминал"});
            put(99,new String[]{"Documentary","Документальный"});
            put(18,new String[]{"Drama","Драма"});
            put(10751,new String[]{"Family","Семейный"});
            put(14,new String[]{"Fantasy","Фэнтези"});
            put(36,new String[]{"History","История"});
            put(27,new String[]{"Horror","Ужасы"});
            put(10402,new String[]{"Music","Музыка"});
            put(9648,new String[]{"Mystery","Детектив"});
            put(10749,new String[]{"Romance","Мелодрама"});
            put(878,new String[]{"Science fiction","Фантастика"});
            put(10770,new String[]{"TV movie","Телевизионный фильм"});
            put(53,new String[]{"Thriller","Триллер"});
            put(10752,new String[]{"War","Военный"});
            put(37,new String[]{"Western","Вестерн"});

            //who knows
            put(105,new String[]{"Disaster","Катастрофа"});
            put(82,new String[]{"Eastern","Западный"});
            put(2916,new String[]{"Erotic","Эротика"});
            put(10750,new String[]{"Fan film","Fan film"});
            put(10753,new String[]{"Film noir","Фильм Нуар"});
            put(878,new String[]{"Fiction","Фэнтези"});
            put(10769,new String[]{"Foreign","Иностранный"});
            put(10595,new String[]{"Holiday","Праздничный"});
            put(10756,new String[]{"Indie","Инди"});
            put(22,new String[]{"Musical","Мюзикл"});
            put(10754,new String[]{"Neo-noir","Нео Нуар"});
            put(1115,new String[]{"Road movie","Дорожное кино"});
            put(10755,new String[]{"Short","Короткий"});
            put(9805,new String[]{"Sport","Спорт"});
            put(10758,new String[]{"Sporting event","Спортивное событие"});
            put(10757,new String[]{"Sport film","Спортивный фильм"});
            put(10748,new String[]{"Suspense","Неизвестный"});
        }
    };
}
