package com.mproduits.utiles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Classe de gestion simplifiée des dates Java, voiçi les fonctionnalités proposées:
 * <ul>
 * <li>conversions</li>
 * <li>comparaisons</li>
 * <li>opérations</li>
 * <li>recuperation</li>
 * </ul>
 * @author v.carruesco
 * @version 1.0 - 23/08/2011 12:21:00
 * @see java.util.Date;
 * @category gestion des dates
 */
public class IdleDate extends Date {


	private static final long serialVersionUID = -1875981288045712454L;

	/*****************/
	/**	CONVERSIONS	**/
	/*****************/
	
	/**
	 * Convertis une Chaine en Date
	 * @param <String> chaine a converir (ex : "12/11/2011")
	 * @param <String> format de la chaine a converir (ex :pour  "12/11/2011" le format est "dd/MM/yyy")
	 * @return <Date> date
	 */
	public static IdleDate parseString(String chaine,String format) {
		SimpleDateFormat typeFormat = new SimpleDateFormat( format );
		Date maDateFinale = new Date();
		try {
			typeFormat.setLenient(false);
			maDateFinale = typeFormat.parse(chaine);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		IdleDate date = new IdleDate();
		date.setTime(maDateFinale.getTime());
		return date ;
	}
	
	/**
	 * Exprime une date sous forme de chaine (ex : "12/12/2011 14:23:10")
	 * @param <String> format de sortie (ex : pour "12/12/2011 14:23:10" est "dd/MM/yyyy kk:mm:ss")
	 * @return <String> chaine de la date
	 */
	public String toString(String format) {
        
                    if("".equalsIgnoreCase(format)){format = "dd/MM/yyyy kk:mm:ss";}
		SimpleDateFormat formatDateJour = new SimpleDateFormat(format);
                
		String dateFormat = formatDateJour.format(this); 
		return dateFormat;
	}
	
	/**
	 * Exprime une date sous forme de chaine (ex : "12/12/2011 14:23:10")
	 * @param <Date> Date a exprimer sous forme de chaine
	 * @param <String> format de sortie (ex : pour "12/12/2011 14:23:10" est "dd/MM/yyyy kk:mm:ss")
	 * @return <String> chaine de la date
	 */
	public static String toString(Date date,String format) {
		if("".equalsIgnoreCase(format)){format = "dd/MM/yyyy kk:mm:ss";}
		SimpleDateFormat formatDateJour = new SimpleDateFormat(format);
		String dateFormat = formatDateJour.format(date); 
		return dateFormat;
	}
	
	
	/*********************/
	/**	COMPARAISONS	**/
	/*********************/
	
	/**
	 * Compare deux dates et retour le nombre de millisecondes,minutes,heures,jours,mois et années de différence
	 * @param <Date> date a comparer avec l'objet courant
	 * @return <HashMap> HashMap<String,Integer> contenant les millisecondes,minutes,heures,jours,mois et 
	 * années de différence ex : myIdleDate.difference(new Date()).get("MONTH") <br/>
	 * <ul>
	 * <li><strong>MILLISECOND :</strong> nombres de millisecondes de différences </li>
	 * <li><strong>SECOND :</strong> nombres de secondes de différences </li>
	 * <li><strong>MINUTE :</strong> nombres de minutes de différences </li>
	 * <li><strong>HOUR :</strong> nombres d'heures de différences </li>
	 * <li><strong>DAY :</strong> nombres de jours de différences </li>
	 * <li><strong>MONTH :</strong> nombres de mois de différences </li>
	 * <li><strong>YEAR :</strong> nombres d'années de différences </li>
	 * </ul>
	 */
	public HashMap<String,Long> difference(Date date){
		HashMap<String,Long> result = new HashMap<String,Long>();
		long differenceTime = 0;
		
		if(this.isOlder(date)){
			differenceTime = date.getTime()-this.getTime();
		}else{
			differenceTime = this.getTime()-date.getTime();
		}
		
		

		result.put("MILLISECOND", differenceTime);
		result.put("SECOND", differenceTime/1000);
		result.put("MINUTE", (differenceTime/1000)/60);
		result.put("HOUR", ((differenceTime/1000)/60)/60);
		result.put("DAY", (((differenceTime/1000)/60)/60)/24);
		result.put("MONTH", ((((differenceTime/1000)/60)/60)/24)/30);
		result.put("YEAR",(((((differenceTime/1000)/60)/60)/24)/30)/12);

		return result;
	}
	
	/**
	 * Compare la date courante avec une autre date
	 * @param <Date> date à comparer a l'objet courant
	 * @return <boolean> si la date courante est plus vielle que la date indiquée en parametre, retourne true, sinon retourne false
	 */
	public boolean isOlder(Date date){
		boolean result = false;
		if(date.getTime()>this.getTime()){
			result = true;
		}
		return result;
	}

	/**
	 * Compare la date courante avec une autre date
	 * @param <Date> date1 à comparer à date2
	 * @param <Date> date2 à comparer à date1
	 * @return <boolean> si la date1 est plus vielle que la date2 , retourne true, sinon retourne false
	 */
	public static boolean isOlder(Date date1,Date date2){
		boolean result = false;
		if(date2.getTime()<date1.getTime()){
			result = true;
		}
		return result;
	}

	/*****************/
	/**	OPERATIONS	**/
	/*****************/
	
	/**
	 * Ajoute un nombre de millisecondes a la date courante
	 * @param <int> nombre de millisecondes à ajouter
	 */
	public void addMillisecond(int millisecond){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.MILLISECOND, millisecond);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre de millisecondes a la date courante
	 * @param <int> nombre de millisecondes à soustraire
	 */
	public void removeMillisecond(int millisecond){
		addMillisecond(-millisecond);
	}
	
	/**
	 * Ajoute un nombre de secondes a la date courante
	 * @param <int> nombre de secondes à ajouter
	 */
	public void addSecond(int second){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.SECOND, second);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre de secondes a la date courante
	 * @param <int> nombre de secondes à soustraire
	 */
	public void removeSecond(int second){
		addSecond(-second);
	}
	
	/**
	 * Ajoute un nombre de minutes a la date courante
	 * @param <int> nombre de minutes à ajouter
	 */
	public void addMinute(int minute){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.MINUTE, minute);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre de minutes a la date courante
	 * @param <int> nombre de minutes à soustraire
	 */
	public void removeMinute(int minute){
		addMinute(-minute);
	}
	
	/**
	 * Ajoute un nombre d'heures a la date courante
	 * @param <int> nombre d'heures à ajouter
	 */
	public void addHour(int hour){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.HOUR, hour);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre d'heures a la date courante
	 * @param <int> nombre d'heures à soustraire
	 */
	public void removeHour(int hour){
		addHour(-hour);
	}
	
	/**
	 * Ajoute un nombre de jours a la date courante
	 * @param <int> nombre de jours à ajouter
	 */
	public  void addDay(int days){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.DATE, days);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre de jours a la date courante
	 * @param <int> nombre de jours à soustraire
	 */
	public  void removeDay(int days){
		addDay(-days);
	}
        
	
	/**
	 * Ajoute un nombre de mois a la date courante
	 * @param <int> nombre de mois à ajouter
	 */
	public void addMonth(int month){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.MONTH, month);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre de mois a la date courante
	 * @param <int> nombre de mois à soustraire
	 */
	public void removeMonth(int month){
		addMonth(-month);
	}
	
	/**
	 * Ajoute un nombre d'années a la date courante
	 * @param <int> nombre d'années à ajouter
	 */
	public void addYear(int year){
		GregorianCalendar calendar = new java.util.GregorianCalendar(); 
		calendar.setTime(this); 
		calendar.add (Calendar.YEAR, year);
		this.setTime(calendar.getTime().getTime());
	}
	
	/**
	 * Soustrait un nombre d'années a la date courante
	 * @param <int> nombre d'années à soustraire
	 */
	public void removeYear(int year){
		addYear(-year);
	}
	
	/*****************/
	/**	ACCESSEURS	**/
	/*****************/
	
	/**
	 * Retourne le nombre de millisecondes de la date courante
	 * @return <int> nombre de millisecondes de la date courante
	 */
	public int getMillisecond(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.MILLISECOND);
	}
	
	/**
	 * Retourne le nombre de secondes de la date courante
	 * @return <int> nombre de secondes de la date courante
	 */
	public int getSecond(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.SECOND);
	}
	
	/**
	 * Retourne le nombre de minutes de la date courante
	 * @return <int> nombre de minutes de la date courante
	 */
	public int getMinute(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.MINUTE);
	}
	
	/**
	 * Retourne l'heure de la date courante
	 * @return <int> heure de la date courante
	 */
	public int getHour(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.HOUR);
	}
	
	/**
	 * Retourne le jour du mois de la date courante
	 * @return <int> jour du mois de la date courante
	 */
	public int getDayMonth(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * Retourne le mois de la date courante
	 * @return <int> mois de la date courante
	 */
	public int getMonth(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.MONTH)+1;
	}
	
	/**
	 * Retourne l'année de la date courante
	 * @return <int> année de la date courante
	 */
	public int getYear(){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(this); 
		return calendar.get(Calendar.YEAR);
	}
	
	/**
	 * Retourne le nombre de millisecondes de la date courante
	 * @return <int> nombre de millisecondes de la date courante
	 */
	public static int getMillisecond(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.MILLISECOND);
	}
	
	/**
	 * Retourne le nombre de secondes de la date courante
	 * @return <int> nombre de secondes de la date courante
	 */
	public static int getSecond(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.SECOND);
	}
	
	/**
	 * Retourne le nombre de minutes de la date courante
	 * @return <int> nombre de minutes de la date courante
	 */
	public static int getMinute(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.MINUTE);
	}
	
	/**
	 * Retourne l'heure de la date courante
	 * @return <int> heure de la date courante
	 */
	public static int getHour(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.HOUR);
	}
	
	/**
	 * Retourne le jour du mois de la date courante
	 * @return <int> jour du mois de la date courante
	 */
	public static int getDayMonth(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * Retourne le mois de la date courante
	 * @return <int> mois de la date courante
	 */
	public static int getMonth(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.MONTH)+1;
	}
	
	/**
	 * Retourne l'année de la date courante
	 * @return <int> année de la date courante
	 */
	public static int getYear(Date date){
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		calendar.setTime(date); 
		return calendar.get(Calendar.YEAR);
	}
	
	/**
	 * Retourne le timestamp de l'objet courant sous forme de long
	 * @return <long> timestamp
	 */
	public long getTimestamp(){
		return this.getTime();
	}
        
  public static int getLastDayInMonth(int year, int month, int day) {
  Calendar calendar = Calendar.getInstance();
  calendar.set(year, month, day);
  int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
  return maxDay;
}
  
  public static LocalDate getDateForLocalDate(Date date){
       
        // Méthode 1 : Conversion directe (recommandée)
        LocalDate localDate = date.toInstant()
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalDate();
        
        System.out.println("Date originale : " + date);
        System.out.println("LocalDate converti : " + localDate);
        
        // Méthode 2 : En spécifiant une zone horaire spécifique
        LocalDate localDateUTC = date.toInstant()
                                     .atZone(ZoneId.of("UTC"))
                                     .toLocalDate();
        
        return  localDateUTC;
  }
	
}
