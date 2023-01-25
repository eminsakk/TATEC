# TATEC - Token Assignment Based Technical Elective Course Selection

This is a simple application that registers students for technical elective courses based on their token distribution written by using Java Stream API. 

## Installation

### Prerequisites

* Java 8 or higher

### Steps

#### Clone the repository

    git clone https://github.com/eminsakk/TATEC
    cd TATEC

#### Compile and run

    javac *.java
    java Tatec <courses_file> <studentIds_file> <studentTokens_file> <unhappiness_coefficient_number>

## Usage

### Input

* `courses_file` - A file that contains the courses and their capacities. Each line of the file contains a course name and its capacity separated by a comma. The file should be in the following format:

        course1,capacity1
        course2,capacity2
        .
        .
        .
        courseN,capacityN
* `studentIds_file` - A file that contains the student ids. Each line of the file contains a student id. The file should be in the following format:

        studentId1
        studentId2
        .
        .
        .
        studentIdM
* `studentTokens_file` - A file that contains the students token distribution for each course seperated by a comma. Each line of the file contains corresponding tokens of a student with the same index in the `studentIds_file`. The file should be in the following format:

        s1Token1,s1Token2,...,s1TokenN
        s2Token1,s2Token2,...,s2TokenN
        .
        .
        .
        sMToken1,sMToken2,...,sMTokenN
* `unhappiness_coefficient_number` - A number that is used to calculate the unhappiness of a student. The unhappiness of a student for each course is calculated as follows:

        if student does not enrolled to a course he/she spent a token on
            unhappiness = (-100/unhappiness_coefficient_number) * ln(1-token_spent_for_course/100)
            unhappiness = min(unhappiness, 100)
        
        if student is not enrolled to any course he/she spent a token on:
            unhappiness = unhappiness * unhappiness
        
* Average unhappiness of all students is calculated as follows:

        students.stream().
            mapToDouble(s -> s.unHappinnesCalculation(h,courses)).
            sum() / students.size()
### Output

* `admissionOutTATEC.txt` & `admissionOutRANDOM.txt` - A file that contains the courses and the students enrolled to them. Each line of the file contains a course name and the students enrolled to it seperated by a comma. The file should be in the following format:
    
        course1,enrolledStudentId1,enrolledStudentId2,...,enrolledStudentIdX
        course2,enrolledStudentId1,enrolledStudentId2,...,enrolledStudentIdY
        .
        .
        .
        courseN,enrolledStudentId1,enrolledStudentId2,...,enrolledStudentIdZ
    
    #### Normally, each course should be filled up to its capacity. Nevertheless, if there are students who spent the same amount of token on a course with the last student enrolled to the course, they all will be enrolled to the course as well. For example, if the capacity of a course is 10 and the last student enrolled to the course spent 5 tokens on the course, the students who spent 5 tokens on the course will be enrolled to the course as well independent of the capacity of the course.

* `unhappinessOutTATEC.txt` & `unhappinessOutRANDOM.txt` - A file that contains the average unhappiness of all students and unhappiness of each student as separated lines. The file should be in the following format:
    
        averageUnhappiness
        student1Unhappiness
        student2Unhappiness
        .
        .
        .
        studentMUnhappiness
