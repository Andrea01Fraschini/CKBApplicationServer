open util/boolean
open ckb_signatures

// What to describe: 
// [ ] Upload solution 
// [ ] End battle + evaluation
// [X] End of tournament 
// [X] Award badges

// Util functions 
fun getBattleByGroup[g : Group]: set Battle {
    enrolledGroups.g
}

fun getTournamentStudents[t : Tournament]: set Student {
    ((t.hosts).enrolledGroups).members
}

fun getBattleSolutions[b : Battle]: one Solution {
    (b.enrolledGroups).uploadedSolution
}

fun getBadgeTournament[b : Badge]: one Tournament {
    hasBadges.b
}

// General facts 

// Solutions are created by groups 
fact groupSolutions {
    all sl : Solution | some g : Group | sl in g.uploadedSolution
}

// After a badge is assigned, the student can't be elegible for that badge again
fact noDoubleBadges {
    always all s : Student | s.badges & s.elegibleForBadges = none 
}

// Students cannot become elegible for badges of already closed tournaments 
fact achieveOpenTournamentBadges {
    always all b : Badge | all s : Student | let t = hasBadges.b | 
        b in s.elegibleForBadges implies t.status = Open
}

// Student cannot achieve badges fo tournament they didn't participate in 
fact mustPartecipateToTournamentForBadge {
    always all s : Student | some t : Tournament | 
        s.elegibleForBadges in t.hasBadges iff s in getTournamentStudents[t] 
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
    all bg : Badge | some t : Tournament | bg in t.hasBadges
}

// [R8]: The system allows an educator to create a battle within a tournament.
fact battleInTournament {
    all b : Battle | some t : Tournament | b in t.hosts
}

// [R10]: The system allows the educator to make a manual assessment of the students' 
// solution, if specidied in the scoring configurations. 

// [DEBATABLE]
// [R14]: The system must update the personal tournament score of each student, that is
// the sum of all battle scores rreceived in that tournament, at the end of each battle.

// [TODO: Remove, this would need the presence of an educator]
// [R15]: The system allows the educator to close a tournament. 
pred closeTournament[t : Tournament] {
    t.status' = Closed
}

// [R16]: The system should assign a badge to one or more students at the end of the 
// tournament if the students have fulfilled the badge's requirements to achieve it.
fun awardedBadgesForTournament[t : Tournament, s : Student]: set Badge {
    t.hasBadges & s.elegibleForBadges
}

// TODO: solver assigns badges to people who weren't previously elegile.
fact assignOnlySatisfiedBadges {
    always all t : Tournament | (t.status = Open and t.status' = Closed) 
        implies (all s : Student | s.badges' = s.badges + awardedBadgesForTournament[t, s]) 
}


// [R16.1]: Badges should not be assigned until the torunament is over. 
fact noBadgesForOpenTournaments {
    always all t : Tournament | no s : Student | t.status = Open and 
        (s.badges & t.hasBadges) != none
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
    always all s : Student | s.badges in s.badges'
}

// [R26]: Once the submission deadline for a battle has been reached, it cannot be reopened
fact noReopeningBattles {
    always all b : Battle | b.status = Closed implies b.status' = Closed
}



// Force some simulation behaviours

fact forceSimulation {
    some s : Student | s.elegibleForBadges != none
}

fact forceSimulation2 {
    eventually some s : Student | s.badges != none
}

// ------------------------------------

pred show[t: Tournament] {
    t.status = Open; t.status = Open; t.status = Closed
    #Educator < 4 and #Educator > 1
    #Group > 1
    #Student > 1
    #Badge > 2
}

run show for 5 but 1 Tournament, 3 steps
