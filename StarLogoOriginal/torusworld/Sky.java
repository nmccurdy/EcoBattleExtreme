package torusworld;

import javax.media.opengl.GL;


public class Sky
{
	
	private void drawASide(GL gl, int nSteps, float startAngle,
			 float x1, float x2, float y, float height, float z1, float z2, 
			 float cx, float cz, float rUp) {

		    gl.glBegin(GL.GL_QUADS);
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			
			for (int i=0;i<nSteps;i++) {
			
				float iNow = (float)i / (float)nSteps;
				float iNext= (float)(i+1) / (float)nSteps;
				
				float angleFrom = (iNow/2.0f + startAngle)* (float)Math.PI ;
				float angleTo	= (iNext/2.0f + startAngle)* (float)Math.PI ;
				
				float xUpFrom = cx + rUp * (float)Math.cos(angleFrom);
				float xUpTo	= cx +	rUp  * (float)Math.cos(angleTo);
				float zUpFrom = cz + rUp * (float)Math.sin(angleFrom);
				float zUpTo	= cz + rUp 	 * (float)Math.sin(angleTo);
				float xDownFrom = x1 + (x2-x1)*iNow;
				float xDownTo	= x1 + (x2-x1)*iNext;
				float zDownFrom = z1 + (z2-z1)*iNow;
				float zDownTo	= z1 + (z2-z1)*iNext;

 
				gl.glTexCoord2f (iNext, 0.0f); 
				gl.glVertex3f(xDownTo, y, zDownTo);
				
				gl.glTexCoord2f (iNext, 1.0f); 
				gl.glVertex3f(xUpTo, y + height, zUpTo);
				
				gl.glTexCoord2f (iNow, 1.0f); 
				gl.glVertex3f(xUpFrom, y + height, zUpFrom);
				
				gl.glTexCoord2f (iNow, 0.0f); 
				gl.glVertex3f(xDownFrom, y, zDownFrom);
				
			}
				
			gl.glEnd();
		
	}

	
	private void drawATopSide(GL gl, int nSteps, float startAngle,
			 float x1, float x2, float y, float height, float z1, float z2, 
			 float cx, float cz, float rUp) {

		    gl.glBegin(GL.GL_TRIANGLES);
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			
			for (int i=0;i<nSteps;i++) {
			
				float iNow = (float)i / (float)nSteps;
				float iNext= (float)(i+1) / (float)nSteps;
				
				float angleFrom = (iNow/2.0f + startAngle)* (float)Math.PI ;
				float angleTo	= (iNext/2.0f + startAngle)* (float)Math.PI ;
				
				float xUpFrom = cx + rUp * (float)Math.cos(angleFrom);
				float xUpTo	= cx +	rUp  * (float)Math.cos(angleTo);
				float zUpFrom = cz + rUp * (float)Math.sin(angleFrom);
				float zUpTo	= cz + rUp 	 * (float)Math.sin(angleTo);
				
				float xTFrom = x1 + (x2-x1)*iNow;
				float xTTo	= x1 + (x2-x1)*iNext;
				float zTFrom = z1 + (z2-z1)*iNow;
				float zTTo	= z1 + (z2-z1)*iNext;

				gl.glTexCoord2f (xTFrom, zTFrom); 
				gl.glVertex3f(xUpFrom, y + height, zUpFrom);

				gl.glTexCoord2f (xTTo, zTTo); 
				gl.glVertex3f(xUpTo, y + height, zUpTo);

				gl.glTexCoord2f (0.5f, 0.5f); 
				gl.glVertex3f(cx, y + 2.0f*height, cz);
				
				
			}
				
			gl.glEnd();
		
	}
	
	
	
    public void loadTextures()
    {
        TextureManager.createTexture("TorusWorld.sky.left",  TorusWorld.class.getResource("textures/left.png"));
        TextureManager.createTexture("TorusWorld.sky.right", TorusWorld.class.getResource("textures/right.png"));
        TextureManager.createTexture("TorusWorld.sky.bottom", TorusWorld.class.getResource("textures/bottom.png"));
        TextureManager.createTexture("TorusWorld.sky.top", TorusWorld.class.getResource("textures/top.png"));
        TextureManager.createTexture("TorusWorld.sky.front", TorusWorld.class.getResource("textures/front.png"));
        TextureManager.createTexture("TorusWorld.sky.back", TorusWorld.class.getResource("textures/back.png"));
    }    

    public void createSkyBox(GL gl,
			     float x, float y, float z,
			     float width, float height, float length)
    {
    
    	gl.glEnable(GL.GL_TEXTURE_2D);
    
    	
    	float rUp = (float)Math.sqrt(width*width + length*length)*2.5f;
    	float xc= (x+width)/2.0f;
    	float zc= (z+width)/2.0f;
    	float horizon = 1.5f;
    	
    	
    	int	  nSteps = 50;
    	
    	
    	// Draw back of Sky Box
    	TextureManager.bindTexture("TorusWorld.sky.back");
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    	    	
    	drawASide(gl, nSteps, -0.75f,
    			 x-horizon*width, x+width+horizon*width, 
    			 y, height,
    			 z-horizon*width, z-horizon*width, 
    			xc, zc, rUp);

    	
    	// Draw Right of Sky Box
    	TextureManager.bindTexture("TorusWorld.sky.right");
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

    	drawASide(gl, nSteps, 1.75f,
    		 x+width+horizon*width, x+width+horizon*width, 
   			 y, height,
   			 z-horizon*width, z+width+horizon*width, 
   			xc, zc, rUp);
    	
    	
    	// Draw front of Sky Box
    	TextureManager.bindTexture("TorusWorld.sky.front");
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    	drawASide(gl, nSteps, 0.25f,
       		 x+width+horizon*width, x-horizon*width, 
      			 y, height,
      			z+width+horizon*width, z+width+horizon*width, 
      			xc, zc, rUp);

    	// Draw Left of Sky Box
    	TextureManager.bindTexture("TorusWorld.sky.left");
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    	drawASide(gl, nSteps, 0.75f,
    				x-horizon*width, x-horizon*width, 
         			y, height,
         			z+width+horizon*width, z-horizon*width, 
         			xc, zc, rUp);
    	
    	// Draw bottom of Sky Box
    	TextureManager.bindTexture("TorusWorld.sky.bottom");
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

    	float horFactor = horizon/(2.0f*horizon+1.0f);
    	
    	
    	gl.glBegin(GL.GL_QUADS);
    	
    	gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(x - width*horizon, 		y, z - length*horizon);
    	gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(x - width*horizon, 		y,	z + length+length*horizon);
    	gl.glTexCoord2f(1.0f-horFactor, 1.0f-horFactor); 	gl.glVertex3f(x, y, z + length);
    	gl.glTexCoord2f(1.0f-horFactor, horFactor); 		gl.glVertex3f(x, y, z);

    	gl.glTexCoord2f(1.0f-horFactor, 1.0f-horFactor); gl.glVertex3f(x, y, z + length);
    	gl.glTexCoord2f(1.0f, 1.0f); 					 gl.glVertex3f(x - width*horizon, y, z + length+length*horizon);
    	gl.glTexCoord2f(0.0f, 1.0f); 					 gl.glVertex3f(x + width + width*horizon, y, z + length+length*horizon);
    	gl.glTexCoord2f(horFactor, 1.0f-horFactor); 	 gl.glVertex3f(x + width, y, z + length);
    	
    	gl.glTexCoord2f(horFactor, 1.0f-horFactor); 	 gl.glVertex3f(x + width, y, z + length);
    	gl.glTexCoord2f(0.0f, 1.0f); 					 gl.glVertex3f(x + width + width*horizon, y, z + length+length*horizon);
    	gl.glTexCoord2f(0.0f, 0.0f); 					 gl.glVertex3f(x + width + width*horizon, y, z - length*horizon);
    	gl.glTexCoord2f(horFactor, horFactor); 			 gl.glVertex3f(x + width, y, z);    	


    	gl.glTexCoord2f(horFactor, horFactor); 			 gl.glVertex3f(x + width, y, z);    	
    	gl.glTexCoord2f(0.0f, 0.0f); 					 gl.glVertex3f(x + width + width*horizon, y, z - length*horizon);
    	gl.glTexCoord2f(1.0f, 0.0f); 					 gl.glVertex3f(x - width*horizon, y, z - length*horizon);
    	gl.glTexCoord2f(1.0f-horFactor, horFactor); 	 gl.glVertex3f(x, y, z);
    	
    	gl.glEnd();
    
    	// Draw Top of Sky Box
    	TextureManager.bindTexture("TorusWorld.sky.top");
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
    	gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    
    	drawATopSide(gl, nSteps, -0.75f,
   			 1.0f, 0.0f, y, height, 1.0f, 1.0f, 
   			 xc, zc, rUp);
    	drawATopSide(gl, nSteps, -0.25f,
      			 0.0f, 0.0f, y, height, 1.0f, 0.0f, 
      			 xc, zc, rUp);
    	drawATopSide(gl, nSteps, 0.25f,
     			 0.0f, 1.0f, y, height, 0.0f, 0.0f, 
     			 xc, zc, rUp);
    	drawATopSide(gl, nSteps, 0.75f,
     			 1.0f, 1.0f, y, height, 0.0f, 1.0f, 
     			 xc, zc, rUp);
    	
   	
    	gl.glDisable(GL.GL_TEXTURE_2D);
    }
}


