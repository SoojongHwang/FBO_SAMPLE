precision mediump float;

uniform sampler2D u_TextureUnit1;
varying vec2 v_TextureCoordinates1;

void main(){
    gl_FragColor = texture2D(u_TextureUnit1, v_TextureCoordinates1);
}