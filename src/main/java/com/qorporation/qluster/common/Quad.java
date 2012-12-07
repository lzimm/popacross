package com.qorporation.qluster.common;

public class Quad<A, B, C, D> {

	private A a;
	private B b;
	private C c;
	private D d;
	
	public Quad(A a, B b, C c, D d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public A a() { return a; }
	public B b() { return b; }
	public C c() { return c; }
	public D d() { return d; }
	
	@Override
	public String toString() {
		return String.format("Quad(%s, %s, %s, %s)", a.toString(), b.toString(), c.toString(), d.toString());
	}

}
