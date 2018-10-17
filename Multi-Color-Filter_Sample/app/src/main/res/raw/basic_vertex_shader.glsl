attribute vec4 a_Position;
attribute vec4 a_TextureInputCoordinates1;

uniform mat4 u_flipMat;

varying vec2 v_TextureCoordinates1;

void main(){
    v_TextureCoordinates1 = (u_flipMat * a_TextureInputCoordinates1).xy;
    gl_Position = a_Position;
}