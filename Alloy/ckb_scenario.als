open util/boolean
open ckb_signatures

// Util functions 
fun getBattleByGroup[g : Group]: one Battle {
    enrolledGroups.g
}

fun getStudentsInTournament[t : Tournament]: set Student {
    ((t.battles).enrolledGroups).members
}

fun getSolutionsByBattle[b : Battle]: set Solution {
    (b.enrolledGroups).currentSolution
}

fun getBattleBySolution[s : Solution]: one Battle {
    enrolledGroups.(currentSolution.s)
}

fun getTournamentManagersByBattle[b : Battle]: set Educator {
    hasPermissionsFor.(battles.b)
}

fun getAwardableBadges[t : Tournament, s : Student]: set Badge {
    t.badges & s.satisfiedBadges
}

fun getGroupsByStudentAndTournament[s : Student, t : Tournament]: set Group {
    (members.s) & (t.battles).enrolledGroups
}

fun sumBattleScoresForGroups[g : Group]: Int {
    sum score : g.battleScore | score
}

// General facts 

// Solutions are created by groups 
fact groupSolutions {
    always all sl : Solution | some g : Group | sl in g.currentSolution
}

// Consider only students in groups 
fact studentsInGroups {
    always all s : Student | some g : Group | s in g.members
}

// After a badge is assigned, the student can't be elegible for that badge again
fact noDoubleBadges {
    always all s : Student | s.awardedBadges & s.satisfiedBadges = none 
}

// Students cannot become elegible for badges of already closed tournaments 
fact achieveOpenTournamentBadges {
    always all b : Badge | all s : Student | let t = badges.b | 
        b in s.satisfiedBadges implies t.status = Open
}

// Student cannot achieve badges fo tournament they didn't participate in 
fact mustPartecipateToTournamentForBadge {
    always all s : Student | all t : Tournament | 
        (s.satisfiedBadges & t.badges) != none implies s in getStudentsInTournament[t] 
}

// Once an educator has manually evaluated a solution, no other educator can evaluate
fact evaluatorRemainsTheSame {
    always all s : Solution | s.evaluatedBy != none implies 
        s.evaluatedBy = s.evaluatedBy'
}

// Manual evaluation implies that the solution has been evaluated
fact evaluationConsistency {
    always all s : Solution | (getBattleBySolution[s].requiresManualEvaluation = True and
        s.evaluatedBy != none) implies s.evaluated = True
}

// If a tournament is closed, there can't be any open battle belonging to that tournament
fact noOpenBattlesForClosedTournament {
    always all t : Tournament | t.status = Closed implies (
        let b = t.battles | b.status = Closed
    )
}

// If a battle is closed, all current solutions must be evaluated
fact allSolutionsEvaluatedForClosedBattle {
    always all b : Battle | b.status = Closed implies 
        (let s = getSolutionsByBattle[b] | s.evaluated = True)
}

// If a group hasn't uploaded a solution or the current one hasn't been evaluated, 
// they should have 0 points
fact noSolutionOrEvaluationNoScore {
    always all g : Group | (g.currentSolution = none or (g.currentSolution).evaluated = False) 
        iff g.battleScore = 0
}

// The score assigned to a specific solution is always the same
fact sameSolutionSameScore {
    always all g : Group |  g.currentSolution = g.currentSolution' implies 
        g.battleScore = g.battleScore' 
}

// -------- REQUIREMENTS ----------

// [R4.1]: The system should not allow a student to enroll in multiple battles within
// the same tournament.
fact studentOneEnrollmentPerBattle {
    all disj g1, g2 : Group | (g1.members & g2.members) != none iff
        getBattleByGroup[g1] != getBattleByGroup[g2]
}

// [R7]: The system allows an educator to create badges within a tournament.
fact badgeInTournament {
    all bg : Badge | some t : Tournament | bg in t.badges
}

// [R8]: The system allows an educator to create a battle within a tournament.
fact battleInTournament {
    all b : Battle | some t : Tournament | b in t.battles
}

// [R10]: The system allows the educator to make a manual assessment of the students' 
// solution, if specidied in the scoring configurations. 

// The educator giving a manual assessment must have permissions to manage battle 
fact evaluatorIsTournamentManager {
    always all s : Solution | s.evaluatedBy != none implies 
        s.evaluatedBy in getTournamentManagersByBattle[getBattleBySolution[s]]
}

// Solutions should only be evaluated at the end of a battle
fact manualEvaluationOnlyAfterBattleCloses {
    always all s : Solution | s.evaluatedBy != none iff (
        let b = getBattleBySolution[s] | b.requiresManualEvaluation = True and b.status = Closed
    )
}

// [R14]: The system must update the personal tournament score of each student, that is
// the sum of all battle scores received in that tournament, at the end of each battle.
fact tournamentScoreEqualToSumOfBattles {
    always all s : Student, t : Tournament | s in getStudentsInTournament[t] implies 
        t.(s.tournamentScore) = sumBattleScoresForGroups[getGroupsByStudentAndTournament[s, t]]
}

// [R15]: The system allows the educator to close a tournament. 
pred closeTournament[t : Tournament] {
    t.status = Open and t.status' = Closed
}

// [R16]: The system should assign a badge to one or more students at the end of the 
// tournament if the students have fulfilled the badge's requirements to achieve it.
fact assignOnlySatisfiedBadges {
    always all t : Tournament | closeTournament[t] implies 
        (all s : Student | s.awardedBadges' = s.awardedBadges + getAwardableBadges[t, s]) 
}

// [R16.1]: Badges should not be assigned until the torunament is over. 
fact noBadgesForOpenTournaments {
    always all t : Tournament | no s : Student | t.status = Open and 
        (s.awardedBadges & t.badges) != none
}

// [R18]: The system should not take into considerations solutions outside the 
// sumbission deadlines (aka after a battle ends)
fact noUploadsAfterBattleEnd {
    always all b : Battle | b.status' = Closed implies  
        getSolutionsByBattle[b] = (b.enrolledGroups).currentSolution' 
}

// [R20]: The system should not allow groups that don't meet battle group size 
// requirements to participate.
fact correctGroupSize {
    all g : Group | let b = getBattleByGroup[g] |
        #g.members >= b.groupsMinSize and #g.members <= b.groupsMaxSize
}

// [R22]: Every tournament should have one and only one owner. 
fact singleTournamentOwner {
    all t : Tournament | one e : Educator | t in e.owns
}

// [R23]: Every educator that owns a tournament should have permission to manage that 
// tournament.
fact tournamentOwnership {
    all e : Educator | e.owns in e.hasPermissionsFor
}

// Group owner is part of the group members. 
fact groupOwnership {
    all s : Student | all g : Group | s in g.owner implies s in g.members
}

// [R24]: Once a tournament has been closed, it cannot be reopened
fact noReopeningTournaments {
    always all t : Tournament | t.status = Closed implies t.status' = Closed
}

// [R25]: Once a badge has been assigned to a student, the student cannot loose that badge. 
fact badgesCannotBeLost {
    always all s : Student | s.awardedBadges in s.awardedBadges'
}

// [R26]: Once the submission deadline for a battle has been reached, it cannot be reopened
fact noReopeningBattles {
    always all b : Battle | b.status = Closed implies b.status' = Closed
}

// Force some simulation behaviours
fact forceSimulation {
    // Force at least one manually evaluated solution
    (eventually some s : Solution | s.evaluatedBy != none) and
    // Force at least one battle that does not require manual evaluation
    (always some b : Battle | getSolutionsByBattle[b] != none and b.requiresManualEvaluation = False) and
    // Force at least one student achieving a badge
    (eventually some st : Student | st.awardedBadges != none) and
    // Some solution is not evaluated at some point 
    (eventually some s : Solution | s.evaluated = False)
}

// ---------------- GOALS -----------------------
// [G1]: Students want to participate as groups (or by themselves) in coding battles 
assert studentsParticipateToBattles {
    some s : Student | some g : Group | some b : Battle |
        g in b.enrolledGroups and 
        s in g.members and #g.members >= 1 
}

check studentsParticipateToBattles

// [G5] Educators want to create and customize gamification badges that will be
// automatically assigned to students.
assert badgesCanBeAssignedToStudents {
    eventually some s : Student | some b : Badge | 
        b in s.awardedBadges
}

check badgesCanBeAssignedToStudents

// [G6]: Educators want the platform to provide automated assessments of students'
// work while also enabling manual evaluations.
assert computePointsForBattles {
    eventually all g : Group |
        g.battleScore != 0 iff (g.currentSolution != none and g.currentSolution.evaluated = True)
}

check computePointsForBattles

assert pointsAssignedForTorunaments {
    eventually all s : Student | all t : Tournament | 
        let g = getGroupsByStudentAndTournament[s, t] |
            s in getStudentsInTournament[t] and t.status = Closed implies 
            t.(s.tournamentScore) = sumBattleScoresForGroups[g]
}

check pointsAssignedForTorunaments

assert automatedAssessment {
    eventually some s : Solution | 
        s.evaluated = True
}

check automatedAssessment

assert allowManualAssessment {
    eventually all s : Solution | let b = getBattleBySolution[s] |
        b.requiresManualEvaluation = True and b.status = Closed implies some e : Educator | 
            s.evaluatedBy = e and e in getTournamentManagersByBattle[b]
}

check allowManualAssessment

// ------------------------------------

pred show[t: Tournament, b : Battle] {
    t.status = Open; t.status = Open; t.status = Closed
    b.status = Open; b.status = Open
    #Educator < 4 and #Educator > 1
    #Group > 2
    #Student > 2
    #Badge > 2
    #Battle > 1
}

run show for 15 but 2 Tournament, 3 Battle, 3 steps

// UPGRADES: 
// - when a group uploads a solution, another group cannot "steal" it