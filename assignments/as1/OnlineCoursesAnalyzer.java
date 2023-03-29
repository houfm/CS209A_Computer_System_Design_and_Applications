import org.w3c.dom.ls.LSException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPath;
import java.util.*;
import java.util.stream.Collectors;




/**
 *
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String,Integer> ans = courses.stream().
                collect(Collectors.groupingBy(Course::getInstitution,Collectors.summingInt(Course::getParticipants)));
        return ans;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String,Integer> ans = courses.stream().
                collect(Collectors.groupingBy(Course::getInstAndSubject,Collectors.summingInt(Course::getParticipants)));
        ans = ans.entrySet().stream().sorted((p1, p2) -> p2.getValue().compareTo(p1.getValue())).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)->e1, LinkedHashMap::new));
        return ans;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String,List<List<String>>> ans = new HashMap<>();
        // where the key is the name of the instructor (without quotation marks)
        // while the value is a list containing 2-course lists
        for (Course course : courses) {
            String[] instructorList = course.instructors.split(", ");
            String inst=instructorList[0];
            if (instructorList.length == 1) {
                // independent
                if (!ans.containsKey(inst)) {
                    // the instructor does not exist
                    List<String> list1 = new ArrayList<>();
                    List<String> list2 = new ArrayList<>();
                    list1.add(course.title);
                    List<List<String>> list = new ArrayList<>();
                    list.add(list1);
                    list.add(list2);
                    ans.put(inst,list);
                } else {
                    if (!ans.get(inst).get(0).contains(course.title)) {
                        ans.get(inst).get(0).add(course.title);
                    }
                    Collections.sort(ans.get(inst).get(0));
                }
            } else {
                // co-developed
                for (int i = 0; i < instructorList.length; i++) {
                    inst = instructorList[i];
                    if (!ans.containsKey(inst)) {
                        // the instructor does not exist
                        List<String> list1 = new ArrayList<>();
                        List<String> list2 = new ArrayList<>();
                        list2.add(course.title);
                        List<List<String>> list = new ArrayList<>();
                        list.add(list1);
                        list.add(list2);
                        ans.put(inst,list);
                    } else {
                        if (!ans.get(inst).get(1).contains(course.title)) {
                            ans.get(inst).get(1).add(course.title);
                        }
                        Collections.sort(ans.get(inst).get(1));
                    }
                }
            }
        }
        return ans;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        // returns the top K courses (parameter topK)
        // by the given criterion (parameter by).
        Map<String,Double> cnt = new HashMap<>();
        List<String> ans = new ArrayList<>();
        // return the title
        switch (by) {
            case "hours":
                // the results should be courses sorted by
                // descending order of Total Course Hours
                for (Course cur : courses) {
                    if (cnt.containsKey(cur.title)) {
                        if (cur.getTotalHours() > cnt.get(cur.title)) {
                            cnt.replace(cur.title, cur.getTotalHours());
                        }
                    } else {
                        cnt.put(cur.title, cur.getTotalHours());
                    }
                }
                break;
            default:
                for (Course cur : courses) {
                    if (cnt.containsKey(cur.title)) {
                        if (cur.getParticipants() > cnt.get(cur.title)) {
                            cnt.replace(cur.title, (double)cur.getParticipants());
                        }
                    } else {
                        cnt.put(cur.title, (double)cur.getParticipants());
                    }
                }
        }
        cnt = cnt.entrySet().stream().sorted((e1,e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
        String[] tmpAns = cnt.keySet().toArray(new String[cnt.size()]);
        for (int i = 0; i < topK; i++) {
            ans.add(tmpAns[i]);
        }
        return ans;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> ans = new ArrayList<>();
        for (Course cur : courses) {
            if (cur.getSubject().toUpperCase().contains(courseSubject.toUpperCase()) &&
                    cur.getPercentAudited() >= percentAudited &&
                    cur.getTotalHours() <= totalCourseHours &&
                    !ans.contains(cur.title)) {
                ans.add(cur.title);
            }
        }
        ans = ans.stream().sorted().collect(Collectors.toList());
        return ans;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        // gender: 0-female, 1-male
        // isBachelorOrHigher: 0-Not get bachelor degree, 1- Bachelor degree or higher
        Map<String,Double> avgAge = courses.stream().
                collect(Collectors.groupingBy(Course::getNumber,Collectors.averagingDouble(Course::getMedianAge)));
        Map<String,Double> avgGender = courses.stream().
                collect(Collectors.groupingBy(Course::getNumber,Collectors.averagingDouble(Course::getPercentMale)));
        Map<String,Double> avgDegree = courses.stream().
                collect(Collectors.groupingBy(Course::getNumber,Collectors.averagingDouble(Course::getPercentDegree)));
        Map<String,Double> tmp = new HashMap<>();
        // tmp: course_number-avg
        for (String cur :
                avgAge.keySet()) {
            double sim = (age - avgAge.get(cur)) * (age - avgAge.get(cur)) +
                    (gender*100 - avgGender.get(cur)) * (gender*100 - avgGender.get(cur)) +
                    (isBachelorOrHigher*100 - avgDegree.get(cur)) * (isBachelorOrHigher*100 - avgDegree.get(cur));
//            if (!tmp.containsKey(cur)) {
                tmp.put(cur,sim);
                // the if clause is duplicate
//            }
        }
        tmp = tmp.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
        // sort number-avg
        Map<String, String> title = new HashMap<>();
        // title: number-title
        Map<String, Date> launchDate = new HashMap<>();
        // launchDate: number-launchDate
        // one number - more than one title
        // REQUIRE: return the course title with the latest Launch Date
        for (Course cur : courses) {
            if (!title.containsKey(cur.number)) {
                title.put(cur.number, cur.title);
                launchDate.put(cur.number, cur.launchDate);
            } else {
                if (cur.launchDate.after(launchDate.get(cur.number))) {
                    // need the latest
                    title.replace(cur.number, cur.title);
                    launchDate.replace(cur.number, cur.launchDate);
                }
            }
        }
        // REQUIRE: the same course title can only occur once in the list
        Map<String,Double> titleAvg = new HashMap<>();
        // title-avg
        // as one title can only occur once in the list, let the largest avg occur
        for (String cur : tmp.keySet()) {
            // tmp key: number
            if (titleAvg.containsKey(title.get(cur))) {
//                if (tmp.get(cur) > titleAvg.get(title.get(cur))) {
//                    // larger avg
//                    titleAvg.replace(title.get(cur), tmp.get(cur));
//                }
            } else {
                titleAvg.put(title.get(cur), tmp.get(cur));
            }
        }
        //  REQUIRE: If two courses have the same similarity values,
        //  then they should be sorted by alphabetical order of their titles.
        titleAvg = titleAvg.entrySet().stream().sorted((p1, p2) -> p1.getValue().compareTo(p2.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2) -> e1, LinkedHashMap::new));
        List<String> ans = new ArrayList<>();
        String[] tmpAns = titleAvg.keySet().toArray(new String[titleAvg.size()]);
        for (int i = 0; i < 10; i++) {
            ans.add(tmpAns[i]);
        }
        return ans;
    }

}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }

    public String getInstAndSubject() {
        return institution + "-" + subject;
    }
    public String getInstitution() {
        return institution;
    }

    public String getNumber() {
        return number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getSubject() {
        return subject;
    }

    public int getYear() {
        return year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public int getAudited() {
        return audited;
    }

    public int getCertified() {
        return certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }
}
