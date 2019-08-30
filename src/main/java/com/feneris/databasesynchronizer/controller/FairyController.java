package com.feneris.databasesynchronizer.controller;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.company.Company;
import org.joda.time.DateTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Artur
 */
public class FairyController {


    private String recordPattern;
    private int count;
    private int actualCount;

    private String seperator = ";";
    private Fairy fairy = Fairy.create();
    private Random random = new Random();
    private DateFormat dateFormater;
    private int[] tab = {0, 0};
    private HashMap<String, String> hMap = new HashMap<String, String>() {
        {
            put("person.firstName", "firstName"); //Imię
            put("person.fullName", "");//Imię i Nazwisko wspólnie
            put("person.middleName", "middleName");//Drugie imię
            put("person.lastName", "lastName");//Nazwisko
            put("person.address", "getAddress");//Adres
            put("person.email", "email");//Adres email
            put("person.nationalIdnNo", "nationalIdentificationNumber");//Identyfikator personalny
            put("person.nationalIdnCardNo", "nationalIdentityCardNumber");//Identyfikator dokumentu
            put("person.passportNo", "passportNumber");//Numer paszportu
            put("person.telephone", "telephoneNumber");//Numer telefonu
            put("person.username", "username");//Użytkownik
            put("person.password", "password");//Hasło
            put("person.gender", "sex");//Płeć
            put("person.age", "age");//Wiek
            put("person.birthDate", "dateOfBirth");//Data urodzenia
            put("address.city", "getCity");//Miasto
            put("address.postalCode", "getPostalCode");//Kod pocztowy
            put("address.street", "street");//Ulica
            put("address.streetNo", "streetNumber");//Numer domu
            put("company.domain", "domain");//Domena internetowa
            put("company.email", "email");// Adres email korporacyjny
            put("company.name", "name");//Nazwa firmy
            put("company.url", "url");//Strona internetowa firmy
            put("company.vatIdnNo", "vatIdentificationNumber");//Identyfikator firmowy
            put("text.latinSentence(#P EMPTY or INTEGER)", "latinSentence");//Zdanie o podanej liczbie słów
            put("text.latinWord(#P EMPTY or INTEGER)", "latinWord");//Wyraz o podanej liczbie liter
            put("text.loremIpsum", "loremIpsum");// Lorem ipsum
            put("text.paragraph(#P EMPTY or INTEGER)", "paragraph");//Paragraf
            put("text.randomString(#P INTEGER)", "randomString");//Losowe znaki o podanej maksymalnej długości
            put("text.sentence(#P EMPTY or INTEGER)", "sentence");// Losowa liczba słów o maksymalnej ilości
            put("text.text", "text");//Losowy tekst
            put("text.word(#P EMPTY or INTEGER)", "word");//Losowe słowo
            put("date.randomFutureDate(#P YEAR)", "randomDateInTheFuture");//Losowa data w przyszłości nie przekraczająca podanego roku
            put("date.randomPastDate(#P YEAR)", "randomDateInThePast");//Losowa data w przeszłości nie przekraczająca podanego roku
            put("date.randomDateBetweenYears(#P YEAR and YEAR)", "randomDateBetweenYears");//Losowa data z przedziału czasowego
            put("number.ip", "ipAddress");//Losowy adres IP
            put("number.positiveInteger", "nextInt");//Liczba dodatnia całkowita o małym zakresie
            put("number.negativeInteger", "nextInt");//Liczba ujemna całkowita o małym zakresie
            put("number.positiveDouble", "nextDouble");//Liczba dodatnia dziesiętna
            put("number.negativeDouble", "nextDouble");//Liczba ujemna dziesiętna
            put("number.positiveLong", "nextLong");//Liczba dodatnia całkowita o dużym zakresie
            put("number.negativeLong", "nextLong");//Liczba ujemna całkowita o dużym zakresie
            put("number.boolean", "nextBoolean");//Zwraca prawdę lub fałsz
            put("number.procent", "nextDouble");//Procent
        }
    };

    public FairyController(String dateFormat) {
        dateFormater = new SimpleDateFormat(dateFormat);
    }

    public void setRecordPattern(String recordPattern) {
        String[] splited = recordPattern.split(seperator);
        for (String s : splited) {
            if (hMap.containsKey(s)) {
                continue;
            } else if (!hMap.containsKey(s)) {
                if (s.contains("#P")) {
                    String startString = s.substring(0, s.indexOf("#P"));
                    boolean isFlag = false;
                    for (String ss : hMap.keySet()) {
                        if (ss.contains(startString)) {
                            isFlag = true;
                        }
                    }
                    if (isFlag) {
                        continue;
                    } else {
                        throw new IllegalArgumentException(s + " is not a record pattern parameter");
                    }
                } else {
                    throw new IllegalArgumentException(s + " is not a record pattern parameter");
                }
            }
        }
        this.recordPattern = recordPattern;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSeperator(String seperator) {
        if (seperator.equals("|")) {
            seperator = "\\|";
        } else if (seperator.equals(".")) {
            throw new IllegalArgumentException("Dot is illegal for seperator");
        }

        this.seperator = seperator;
    }

    public List<String> getResult() throws Exception {

        List<String> toReturn = new LinkedList<>();
        for (actualCount = 0; actualCount < count; actualCount++) {

            toReturn.add(generateRecord(recordPattern));

        }
        return toReturn;
    }

    public List<String> getOptionList() {
        List<String> toReturn = new LinkedList<>(hMap.keySet());
        toReturn.add("static.(#P STRING)");
        Collections.sort(toReturn);
        return toReturn;
    }

    private String generateRecord(String recordPattern) throws Exception {
        StringBuilder value = new StringBuilder("");

        String[] splitted = recordPattern.split(seperator);
        Company c;
        for (int i = 0; i < splitted.length; i++) {
            String s = splitted[i];
            if (s.contains("person.")) {
                if (s.equals("person.birthDate")) {
                    value.append(useReflectionToDate("io.codearte.jfairy.producer.person.Person", s, fairy.person(), null));
                } else if (s.equals("person.fullName")) {
                    value.append(useReflectionToString("io.codearte.jfairy.producer.person.Person", "person.firstName", fairy.person(), null));
                    value.append(" ");
                    value.append(useReflectionToString("io.codearte.jfairy.producer.person.Person", "person.lastName", fairy.person(), null));
                } else {
                    value.append(useReflectionToString("io.codearte.jfairy.producer.person.Person", s, fairy.person(), null));
                }
            } else if (s.contains("address.")) {
                value.append(useReflectionToString("io.codearte.jfairy.producer.person.Address", s, fairy.person().getAddress(), null));
            } else if (s.contains("company.")) {
                value.append(useReflectionToString("io.codearte.jfairy.producer.company.Company", s, fairy.person().getCompany(), null));
            } else if (s.contains("number.")) {
                if (s.equals("number.ip")) {
                    value.append(useReflectionToString("io.codearte.jfairy.producer.net.NetworkProducer", s, fairy.networkProducer(), null));
                } else {
                    String integer = useReflectionToString("java.util.Random", s, new Random(), null);
                    integer = integer.replaceAll("-", "");
                    if (s.contains("negative")) {
                        value.append("-");
                        value.append(integer);
                    } else if (s.contains("positive")) {
                        value.append(integer);
                    } else if (s.equals("number.procent")) {
                        value.append(Double.parseDouble(integer.substring(0, 4)) * 100);
                        value.append("%");
                    } else {
                        value.append(integer);
                    }
                }
            } else if (s.contains("text.")) {
                String cleanedS = getCleanedString(s);
                String arrgument = getArgumentFromString(s);
                if (arrgument == "") {
                    value.append(useReflectionToString("io.codearte.jfairy.producer.text.TextProducer", cleanedS, fairy.textProducer(), null));
                } else {
                    value.append(useReflectionToString("io.codearte.jfairy.producer.text.TextProducer", cleanedS, fairy.textProducer(), new int[]{Integer.parseInt(arrgument)}, int.class));
                }
            } else if (s.contains("date.")) {
                String cleanedS = getCleanedString(s);
                String arrgument = getArgumentFromString(s);
                if (arrgument.equals("")) {
                    value.append("");
                } else if (arrgument.length() == 8) {
                    int arrg1 = Integer.parseInt(arrgument.substring(0, 4));
                    int arrg2 = Integer.parseInt(arrgument.substring(4, 8));
                    value.append(useReflectionToDate("io.codearte.jfairy.producer.DateProducer", cleanedS, fairy.dateProducer(), new int[]{arrg1, arrg2}, int.class, int.class));
                } else {
                    value.append(useReflectionToDate("io.codearte.jfairy.producer.DateProducer", cleanedS, fairy.dateProducer(), new int[]{Integer.parseInt(arrgument)}, int.class));
                }
            } else if (s.contains("static.(#P STRING)")) {
                value.append(s.substring(18));
            } else {
                throw new IllegalArgumentException("Unknow field value " + s);
            }

            if (i != splitted.length - 1) {
                value.append(seperator);
            }
        }
        return value.toString();
    }

    private String getArgumentFromString(String tempS) {
        Integer i = 0;
        String arrgument = "";
        while (Character.isDigit(tempS.charAt(tempS.length() - (i + 1)))) {
            arrgument = tempS.charAt(tempS.length() - (i + 1)) + arrgument;
            i++;
        }
        return arrgument;
    }

    private String getCleanedString(String tempS) {
        int i = 0;
        while (Character.isDigit(tempS.charAt(tempS.length() - 1))) {
            tempS = tempS.substring(0, tempS.length() - 1);
            i++;
        }
        return tempS;
    }


    private String useReflectionToString(String className, String methodName, Object instance, int[] arguments, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class cl = Class.forName(className);//Pobierz klase obiektu o podanej nazwie
        Method method = cl.getMethod(hMap.get(methodName), parameterTypes);//Pobierz metodę po podanej nazwie
        if (arguments == null) {//Jeżeli nie podano argumentów wywołaj metodę i przekaż wynik
            return method.invoke(instance).toString();
        } else {
            if (arguments.length == 2) {//Jeżeli podano 2 argumentu wywołaj metodę z dwoma argumentami
                int arr1 = arguments[0];
                int arr2 = arguments[1];
                return method.invoke(instance, arr1, arr2).toString();
            } else if (arguments.length == 1) {//Jeżeli podano 1 argument wywołaj metodę z jednym argumentem
                int arr1 = arguments[0];
                return method.invoke(instance, arr1).toString();
            } else {
                throw new IllegalArgumentException("Wrong number of arguments");
            }
        }

    }

    private String useReflectionToDate(String className, String methodName, Object instance, int[] arguments, Class<?>... parameterTypes) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class cl = Class.forName(className);
        Method method = cl.getMethod(hMap.get(methodName), parameterTypes);
        DateTime valueDF;
        if (arguments == null) {
            valueDF = (DateTime) method.invoke(instance);
        } else {
            if (arguments.length == 2) {
                int arr1 = arguments[0];
                int arr2 = arguments[1];
                valueDF = (DateTime) method.invoke(instance, arr1, arr2);
            } else if (arguments.length == 1) {
                int arr1 = arguments[0];
                valueDF = (DateTime) method.invoke(instance, arr1);
            } else {
                throw new IllegalArgumentException("Wrong number of arguments");
            }
        }
        return dateFormater.format(valueDF.toDate()).toString();
    }

}
