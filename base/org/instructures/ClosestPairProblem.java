package org.instructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ClosestPairProblem {
	
	public static void main(String...args) {
		Point p1 = new Point(3);
		Point p2 = new Point(5);
		Point p3 = new Point(4);
		Point p4 = new Point(6);
		ArrayList<Point> points = new ArrayList<Point>();
		points.add(p1);
		points.add(p2);
		points.add(p3);
		points.add(p4);
		System.out.println(allClosestPair(points));
	}
	
	private static class Point {
		private int x;
		
		Point(int x) {
			this.x = x;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}
		
		public int distance(Point o) {
			return Math.abs(this.x - o.x);
		}
		
		@Override
		public String toString() {
			return "Point{" + hashCode() + "} = " + Integer.toString(this.x);
		}

		
	}
	
	private static Set<Set<Point>> allClosestPair(final List<Point> points) {
		HashMap<Integer, HashSet<Set<Point>>> distances = new HashMap<Integer, HashSet<Set<Point>>>();
		int mindist = 0;
		for(Point p1 : points) {
			for(Point p2 : points) {
				if (!p1.equals(p2)) {
					int distance = p1.distance(p2);
					HashSet<Point> pair = new HashSet<Point>();
					pair.add(p1);
					pair.add(p2);
					if (distances.containsKey(distance)) {			
						distances.get(distance).add(pair);
					} else {
						if (distance < mindist || distances.isEmpty()) {
							distances.clear();
							HashSet<Set<Point>> smallestSet = new HashSet<Set<Point>>();
							smallestSet.add(pair);
							distances.put(distance, smallestSet);
							mindist = distance;
						}
					}
				}
			}
		}
		return distances.get(mindist);
	}
	
	private static Set<Point> closestPair(final List<Point> points) {
		Set<Point> closestPair = new HashSet<Point>();
		int min = 0;
		for(int i = 0; i < points.size(); i++) {
			Point p1 = points.get(i);
			for(int j = 0; j < points.size(); j++) {
				Point p2 = points.get(j);
				if (!p1.equals(p2)) {
					int distance = p1.distance(p2);
					if (distance < min  || closestPair.isEmpty()) {
						min = distance;
						if (!closestPair.isEmpty()) {
							closestPair.clear();
						}
						closestPair.add(p1);
						closestPair.add(p2);
					}
				}
					
			}
		}
		return closestPair;
	}

}
