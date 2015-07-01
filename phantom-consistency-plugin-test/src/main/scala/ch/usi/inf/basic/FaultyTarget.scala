package ch.usi.inf.basic

import com.datastax.driver.mapping.annotations.Table

/**
 * @author nosheen
 * 
 * TODO test this with the should compile and should not compile stuff.
 */
class FaultyTarget {
  @Table(name = "main_recipes", writeConsistency = "ONE")val faulty = 
    "Not correct to have an annotation here."
}