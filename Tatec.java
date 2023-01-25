import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Tatec
{
    private static final int CORRECT_TOTAL_TOKEN_PER_STUDENT = 100;
    private static final String OUT_TATEC_UNHAPPY = "unhappyOutTATEC.txt";
    private static final String OUT_TATEC_ADMISSION = "admissionOutTATEC.txt";
    private static final String OUT_RAND_UNHAPPY = "unhappyOutRANDOM.txt";
    private static final String OUT_RAND_ADMISSION = "admissionOutRANDOM.txt";


    public static class SumIsNot100Exception extends Exception
    {
        public SumIsNot100Exception(String errorMsg) {      super(errorMsg);    }
    }


    static class Course
    {
        private String id;
        private int capacity;
        private List<Student> registeredStudents;

        private int actualCapacity;

        public Course(String id,int capacity)
        {
            this.id = id;
            this.capacity = capacity;
            this.registeredStudents = new ArrayList<>();
            this.actualCapacity = capacity;
        }

        private void decrementCapacity()
        {
            this.capacity--;
        }

        public void registerStudent(Student student)
        {
            registeredStudents.add(student);
            decrementCapacity();

        }
        public void registerExtraStudent(Student student)
        {
            registeredStudents.add(student);
        }
        public int  capacity()
        {
            return this.capacity;
        }

        public void resetTheConfigs()
        {
            this.registeredStudents.clear();
            this.capacity = this.actualCapacity;
        }
    }

    static class Student
    {
        private String username;
        private List<Integer> tokens;
        private List<Course> courseTaken;

        private int nonZeroTokenCount;

        double unhappinnes;
        public Student(String username,List<Integer> tokens) throws SumIsNot100Exception {
            this.username = username;
            this.tokens = tokens;



            int sum = this.tokens.stream().mapToInt(Integer::intValue).sum();



            if(sum != 100)
                throw new SumIsNot100Exception("BOOM!");

            this.nonZeroTokenCount = (int) this.tokens.stream().filter(token -> token != 0).count();
            this.courseTaken = new ArrayList<>();
        }

        public void addCourse(Course course)
        {
            courseTaken.add(course);
        }

        public double unHappinnesCalculation(double h, List<Course> courses) {
            double result = courses.stream().filter(c -> !c.registeredStudents.contains(this) && tokens.get(courses.indexOf(c)) != 0)
                    .mapToDouble(course -> (-100 / h) * Math.log(1 - (tokens.get(courses.indexOf(course)) / 100.0)))
                    .map(unHappinnes -> Double.isInfinite(unHappinnes) ? 100 : unHappinnes)
                    .sum();
            if (courseTaken.size() == 0)
                result = result * result;

            this.unhappinnes = result;
            return result;
        }

        public void resetTheConfigs()
        {
            this.courseTaken = new ArrayList<>();
            this.unhappinnes = 0;
        }

    }

    public static void main(String args[])
    {
        if(args.length < 4)
        {
            System.err.println("Not enough arguments!");
            return;
        }

        // File Paths
        String courseFilePath = args[0];
        String studentIdFilePath = args[1];
        String tokenFilePath = args[2];
        double h;

        try { h = Double.parseDouble(args[3]);}
        catch (NumberFormatException ex)
        {
            System.err.println("4th argument is not a double!");
            return;
        }


        Path coursePath = Paths.get(courseFilePath);
        Path studentIDPath = Paths.get(studentIdFilePath);
        Path tokenPath = Paths.get(tokenFilePath);


        try(Stream<String> courseStream = Files.lines(coursePath);
            Stream<String> studentStream = Files.lines(studentIDPath);
            Stream<String> tokenStream = Files.lines(tokenPath);)
        {
            List<String> strCourse = courseStream.flatMap(l -> Arrays.stream(l.split(","))).collect(Collectors.toList());
            List<String> strStudents = studentStream.collect(Collectors.toList());
            List<String> strTokens = tokenStream.collect(Collectors.toList());


            List<Course> courses = Collections.unmodifiableList(IntStream.range(0,strCourse.size()).filter(x -> x % 2 == 0)
                    .mapToObj(i -> new Course(strCourse.get(i),Integer.parseInt(strCourse.get(i + 1))))
                    .collect(Collectors.toList()));



            List<Student> students = Collections.unmodifiableList(IntStream.range(0,strStudents.size())
                    .mapToObj(i -> {
                        try
                        {
                            return new Student(strStudents.get(i),Stream.of(strTokens.get(i).split(",")).map(String::trim).map(Integer::parseInt)
                                    .collect(Collectors.toList()));
                        } catch (SumIsNot100Exception e) {
                            System.out.println(String.format("Student %s does not used 100 tokens",strStudents.get(i)));
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList()));

            IntStream.range(0,courses.size()).forEach(i -> {
                List<Student> sortedStudents = students.stream().filter(s -> s.tokens.get(i) > 0)
                        .sorted((s1,s2) -> s2.tokens.get(i) - s1.tokens.get(i)).collect(Collectors.toList());


                int min = sortedStudents.size() > courses.get(i).capacity() ? courses.get(i).capacity() : sortedStudents.size();
                IntStream.range(0,min).forEach(j -> {
                    sortedStudents.get(j).addCourse(courses.get(i));
                    courses.get(i).registerStudent(sortedStudents.get(j));
                });


                if(min > 0)
                {
                    int minToken = sortedStudents.get(min - 1).tokens.get(i);
                    IntStream.range(min,sortedStudents.size()).filter(j -> sortedStudents.get(j).tokens.get(i) == minToken)
                            .forEach(j-> {
                                sortedStudents.get(j).addCourse(courses.get(i));
                                courses.get(i).registerExtraStudent(sortedStudents.get(j));
                            });
                }
            });


            // Calculate Unhappiness

            //double unhappinnes = courses.stream().mapToDouble(c -> students.stream().mapToDouble(s -> s.unHappinnesCalculation(c,h,courses)).sum()).sum();
            String unHappinnesTatec = Double.toString( students.stream().mapToDouble(s -> s.unHappinnesCalculation(h,courses)).sum() / students.size()) + "\n";


            String tatecOut = courses.stream().map(c -> String.format("%s,%s",c.id,c.registeredStudents.stream().map(s -> s.username).collect(Collectors.joining(","))))
                    .collect(Collectors.joining("\n"));


            String tatecUnHappinnes = students.stream().map(s -> String.format("%.16f",s.unhappinnes)).collect(Collectors.joining("\n"));

            // Write the result to the file using FileOutputStream.
            FileOutputStream tatecAdmissions = new FileOutputStream(OUT_TATEC_ADMISSION);
            tatecAdmissions.write(tatecOut.getBytes());
            tatecAdmissions.close();

            FileOutputStream tatecUnhappinnes = new FileOutputStream(OUT_TATEC_UNHAPPY);
            tatecUnhappinnes.write(unHappinnesTatec.getBytes());
            tatecUnhappinnes.write(tatecUnHappinnes.getBytes());
            tatecUnhappinnes.close();



            // Reset the configs
            students.forEach(Student::resetTheConfigs);
            courses.forEach(Course::resetTheConfigs);


            // Assign randomly students  at most nonzero token count at that student to courses  using streams.
            // The number of students assigned to a course should not exceed the capacity of the course.




            // Randomly assign students to courses

            IntStream.range(0,courses.size()).forEach(i -> {
                List<Student> shuffledStudents = students.stream().filter(s -> s.tokens.get(i) > 0)
                        .collect(Collectors.toList());

                Collections.shuffle(shuffledStudents);

                int min = shuffledStudents.size() > courses.get(i).capacity() ? courses.get(i).capacity() : shuffledStudents.size();
                IntStream.range(0,min).forEach(j -> {
                    shuffledStudents.get(j).addCourse(courses.get(i));
                    courses.get(i).registerStudent(shuffledStudents.get(j));
                });
            });

            // Calculate Unhappiness
            String unHappinnesRandom = Double.toString( students.stream().mapToDouble(s -> s.unHappinnesCalculation(h,courses)).sum() / students.size()) + "\n";

            String randomOut = courses.stream().map(c -> String.format("%s,%s",c.id,c.registeredStudents.stream().map(s -> s.username).collect(Collectors.joining(","))))
                    .collect(Collectors.joining("\n"));

            String randomUnHappinnes = students.stream().map(s -> String.format("%.16f",s.unhappinnes)).collect(Collectors.joining("\n"));

            FileOutputStream randomAdmissions = new FileOutputStream(OUT_RAND_ADMISSION);
            randomAdmissions.write(randomOut.getBytes());
            randomAdmissions.close();

            FileOutputStream randomUnhappinnes = new FileOutputStream(OUT_RAND_UNHAPPY);
            randomUnhappinnes.write(unHappinnesRandom.getBytes());
            randomUnhappinnes.write(randomUnHappinnes.getBytes());
            randomUnhappinnes.close();


        }
        catch (IOException exc)
        {
            System.err.println("Error reading file!");

        }
        catch (RuntimeException exc)
        {
            System.err.println(exc.getCause().getMessage());
            return;
        }
    }

}