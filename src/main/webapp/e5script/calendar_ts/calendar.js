// Title: Tigra Calendar
// URL: http://www.softcomplex.com/products/tigra_calendar/
// Version: 3.3 (American date format)
// Date: 09/01/2005 (mm/dd/yyyy)
// Note: Permission given to use this script in ANY kind of applications if
//    header lines are left unchanged.
// Note: Script consists of two files: calendar?.js and calendar.html

// if two digit year input dates after this year considered 20 century.
var NUM_CENTYEAR = 30;
// is time input control required by default
var BUL_TIMECOMPONENT = true;
// is mills required by default
var BUL_MILLSCOMPONENT = false;
// are year scrolling buttons required by default
var BUL_YEARSCROLL = true;

var calendars = [];
var RE_NUM = /^\-?\d+$/;

function calendar3(workdir, obj_target) {

	// validate input parameters
	if (!obj_target)
		return cal_error("Error calling the calendar: no target control specified");
	if (obj_target.value == null)
		return cal_error("Error calling the calendar: parameter specified is not valid target control");

	// assigning methods
	this.workdir = workdir;
	this.gen_date = cal_gen_date3;
	this.gen_time = cal_gen_time3;
	this.gen_tsmp = cal_gen_tsmp3;
	this.prs_date = cal_prs_date3;
	this.prs_time = cal_prs_time3;
	this.prs_tsmp = cal_prs_tsmp3;
	this.popup    = cal_popup3;

	this.target = obj_target;
	this.time_comp = BUL_TIMECOMPONENT;
	this.mills_comp = BUL_MILLSCOMPONENT;
	this.year_scroll = BUL_YEARSCROLL;
	
	// register in global collections
	this.id = calendars.length;
	calendars[this.id] = this;
}

function cal_popup3 (str_datetime) {
	try
	{
		if (str_datetime) {
			this.dt_current = this.prs_tsmp(str_datetime);
		}
		else {
			this.dt_current = this.prs_tsmp(this.target.value);
			this.dt_selected = this.dt_current;
		}
	} catch (e) {}

	if (!this.dt_current) this.dt_current = new Date();

	var url = window.location.href;
	var pos = url.lastIndexOf( "/" );
	var prefix = url.substring( 0, pos + 1 ) + this.workdir;
	
	var obj_calwindow = window.open(
		prefix + '/calendar.html?datetime=' + this.dt_current.valueOf()+ '&id=' + this.id,
		'Calendar', 'width=200,height='+(this.time_comp ? 215 : 190)+
		',status=no,resizable=no,top=200,left=200,dependent=yes,alwaysRaised=yes'
	);
	obj_calwindow.opener = window;
	obj_calwindow.focus();
}

// timestamp generating function
function cal_gen_tsmp3 (dt_datetime) {
	return(this.gen_date(dt_datetime) + ' ' + this.gen_time(dt_datetime));
}

// date generating function
function cal_gen_date3 (dt_datetime) {
	return (
		dt_datetime.getFullYear() + "-"
		+ (dt_datetime.getMonth() < 9 ? '0' : '') + (dt_datetime.getMonth() + 1) + "-"
		+ (dt_datetime.getDate() < 10 ? '0' : '') + dt_datetime.getDate()
	);
}
// time generating function
function cal_gen_time3 (dt_datetime) {
	var timeStr = (
		(dt_datetime.getHours() < 10 ? '0' : '') + dt_datetime.getHours() + ":"
		+ (dt_datetime.getMinutes() < 10 ? '0' : '') + (dt_datetime.getMinutes()) + ":"
		+ (dt_datetime.getSeconds() < 10 ? '0' : '') + (dt_datetime.getSeconds()) );
	if(this.mills_comp)
		timeStr += "." + (dt_datetime.getMilliseconds());
	return timeStr;
}

// timestamp parsing function
function cal_prs_tsmp3 (str_datetime) {
	// if no parameter specified return current timestamp
	if (!str_datetime)
		return (new Date());

	// if positive integer treat as milliseconds from epoch
	if (RE_NUM.exec(str_datetime))
		return new Date(str_datetime);
		
	// else treat as date in string format
	var arr_datetime = str_datetime.split(' ');
	return this.prs_time(arr_datetime[1], this.prs_date(arr_datetime[0]));
}

// date parsing function
function cal_prs_date3 (str_date) {
	var arr_date = str_date.split('-');

	if (arr_date.length != 3) return alert ("Invalid date format: '" + str_date + "'.\nFormat accepted is yyyy-mm-dd.");
	if (!arr_date[2]) return alert ("Invalid date format: '" + str_date + "'.\nNo day of month value can be found.");
	if (!RE_NUM.exec(arr_date[2])) return alert ("Invalid day of month value: '" + arr_date[2] + "'.\nAllowed values are unsigned integers.");
	if (!arr_date[1]) return alert ("Invalid date format: '" + str_date + "'.\nNo month value can be found.");
	if (!RE_NUM.exec(arr_date[1])) return alert ("Invalid month value: '" + arr_date[1] + "'.\nAllowed values are unsigned integers.");
	if (!arr_date[0]) return alert ("Invalid date format: '" + str_date + "'.\nNo year value can be found.");
	if (!RE_NUM.exec(arr_date[0])) return alert ("Invalid year value: '" + arr_date[0] + "'.\nAllowed values are unsigned integers.");

	var dt_date = new Date();
	dt_date.setDate(1);

	if (arr_date[1] < 1 || arr_date[1] > 12) return alert ("Invalid month value: '" + arr_date[1] + "'.\nAllowed range is 01-12.");
	dt_date.setMonth(arr_date[1]-1);
	 
	if (arr_date[0] < 100) arr_date[0] = Number(arr_date[0]) + (arr_date[0] < NUM_CENTYEAR ? 2000 : 1900);
	dt_date.setFullYear(arr_date[0]);

	var dt_numdays = new Date(arr_date[0], arr_date[1], 0);
	dt_date.setDate(arr_date[2]);
	if (dt_date.getMonth() != (arr_date[1]-1)) return alert ("Invalid day of month value: '" + arr_date[2] + "'.\nAllowed range is 01-"+dt_numdays.getDate()+".");

	return (dt_date)
}

// time parsing function
function cal_prs_time3 (str_time, dt_date) {

	if (!dt_date) return null;
	var arr_time = String(str_time ? str_time : '').split(':');

	if (!arr_time[0]) dt_date.setHours(0);
	else if (RE_NUM.exec(arr_time[0])) 
		if (arr_time[0] < 24) dt_date.setHours(arr_time[0]);
		else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed range is 00-23.");
	else return cal_error ("Invalid hours value: '" + arr_time[0] + "'.\nAllowed values are unsigned integers.");
	
	if (!arr_time[1]) dt_date.setMinutes(0);
	else if (RE_NUM.exec(arr_time[1]))
		if (arr_time[1] < 60) dt_date.setMinutes(arr_time[1]);
		else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed range is 00-59.");
	else return cal_error ("Invalid minutes value: '" + arr_time[1] + "'.\nAllowed values are unsigned integers.");

	var arr_tmp = String(arr_time[2] ? arr_time[2] : '').split('.');

	if (!arr_tmp[0]) dt_date.setSeconds(0);
	else if (RE_NUM.exec(arr_tmp[0]))
		if (arr_tmp[0] < 60) dt_date.setSeconds(arr_tmp[0]);
		else return cal_error ("Invalid seconds value: '" + arr_tmp[0] + "'.\nAllowed range is 00-59.");
	else return cal_error ("Invalid seconds value: '" + arr_tmp[0] + "'.\nAllowed values are unsigned integers.");

	if (!arr_tmp[1]) dt_date.setMilliseconds(0);
	else if (RE_NUM.exec(arr_tmp[1]))
		if (arr_tmp[1] < 1000) dt_date.setMilliseconds(arr_tmp[1]);
		else return cal_error ("Invalid milliseconds value: '" + arr_tmp[1] + "'.\nAllowed range is 00-999.");
	else return cal_error ("Invalid seconds value: '" + arr_tmp[1] + "'.\nAllowed values are unsigned integers.");

	return dt_date;
}

function cal_error (str_message) {
	alert (str_message);
	return null;
}