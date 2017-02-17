/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uib.onlinepeptideshaker.model;

/**
 *
 * @author yfa041
 */
public class RefresherThread extends Thread{
     @Override
        public void run() {
           String databaseResult = veryHugeDatabaseCalculation();
        }
        
        private String veryHugeDatabaseCalculation() {
            
            for (long x=0;x<1000L;x++) {
                System.out.println("com.uib.onlinepeptideshaker.model.RefresherThread.veryHugeDatabaseCalculation()");
            }
            
            
            try {
                Thread.sleep(6000);
            } catch (final InterruptedException ignore) {
                return "interrupted!";
            }
            return "huge!";
        }
}
