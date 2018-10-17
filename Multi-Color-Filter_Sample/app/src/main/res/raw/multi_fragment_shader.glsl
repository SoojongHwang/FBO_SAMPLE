uniform sampler2D u_TextureUnit1;
uniform sampler2D u_TextureUnit2; // lookup texture

uniform int u_Location;

varying mediump vec2 v_TextureCoordinates1;

void main()
{
    lowp vec2 filterLocation;
    filterLocation.x = float(u_Location - 2 * (u_Location / 2)) * 0.5;
    filterLocation.y = float(u_Location / 2) * 0.03125;

    mediump vec4 textureColor = texture2D(u_TextureUnit1, v_TextureCoordinates1);

    mediump float blueColor = textureColor.b * 15.0;

    lowp float quad1 = floor(blueColor);
    lowp float quad2 = ceil(blueColor);

    mediump float offset_red = filterLocation.x + ((textureColor.r * 15.0) / 512.0) + (0.5 / 512.0);
    mediump float offset_green = filterLocation.y + (0.5/512.0) + (15.0/512.0) * textureColor.g;

    mediump vec2 texPos1;
    texPos1.x = quad1 * 0.03125 + offset_red;
    texPos1.y = offset_green;

    mediump vec2 texPos2;
    texPos2.x = quad2 * 0.03125 + offset_red;
    texPos2.y = offset_green;

    mediump vec4 newColor1 = texture2D(u_TextureUnit2, texPos1);
    mediump vec4 newColor2 = texture2D(u_TextureUnit2, texPos2);

    gl_FragColor = mix(newColor1, newColor2, fract(blueColor));
}