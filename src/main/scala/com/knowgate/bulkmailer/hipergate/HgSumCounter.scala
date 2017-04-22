package com.knowgate.bulkmailer.hipergate

import java.util.Arrays
import java.util.Comparator

import com.knowgate.bulkmailer.SumCounter
import com.knowgate.bulkmailer.ClickThrough

import org.judal.storage.DataSource

import scala.collection.mutable.Buffer
import scala.collection.mutable.TreeSet
import scala.collection.mutable.ListBuffer

trait HgSumCounter extends SumCounter {
    
  private var sent : Int = -1
  private var opened : Int = -1
  private var clicks : Int = -1
  private var unique : Int = -1
  
   def sentCount() : Int = sent

   def openedCount() : Int = opened
  
   def clicksCount() : Int = clicks

   def uniqueCount() : Int = unique

   def setSentCount(c: Int) = { sent=c }

   def setOpenedCount(c: Int) = { opened=c }

   def setUniqueCount(c: Int) = { unique=c }

   def setClicksCount(c: Int) = { clicks=c }

   protected def sumCounters(dts: DataSource) : Unit =  {
     val cntr = new HgGroupSumCounter
     cntr.open(dts)
     cntr.sum(this)
     cntr.close     
   }
  
   protected def sortClicksByDate(clks: Array[ClickThrough]) : Int = {
     Arrays.sort(clks, new Comparator[ClickThrough]() { def compare(c1: ClickThrough, c2: ClickThrough) = c1.date().compareTo(c2.date()) })
     clks.length
   }
      
   protected def mergeClicks(clks: Buffer[Array[ClickThrough]]) : Array[ClickThrough] = {
     var cnt = 0;
     clks.foreach(c => cnt += sortClicksByDate(c))
     val clt = new Array[ClickThrough](cnt)
     val cur = new Array[Int](cnt)
     Arrays.fill(cur, 0)
     val heap = new TreeSet[Tuple2[ClickThrough,Int]]()(new Ordering[Tuple2[ClickThrough,Int]]() { def compare(t1: Tuple2[ClickThrough,Int], t2: Tuple2[ClickThrough,Int]) = t1._1.date().compareTo(t2._1.date()) } )
     for (c <- 0 until clks.length) {
       val clk = clks(c)
       if (clk.length>0) {
         heap += new Tuple2(clk(0),c)
         cur(c) += 1
       }
     }
          
     var a = 0
     while(!heap.isEmpty) {
       var tpl = heap.head
       heap -= tpl
       clt(a) = tpl._1
       a += 1
       val c = tpl._2
       val clk = clks(c)
       if (clk.length>cur(c)) {
         heap += new Tuple2(clk(cur(c)),c)
         cur(c) += 1
       }
     }
     clt
   }  
}