vec4 a_color0 : COLOR0;
vec2 a_texcoord0 : TEXCOORD0;
vec2 a_texcoord1 : TEXCOORD1;
vec3 a_position : POSITION;
#ifdef INSTANCING
vec4 i_data0 : TEXCOORD8;
vec4 i_data1 : TEXCOORD7;
vec4 i_data2 : TEXCOORD6;
vec4 i_data3 : TEXCOORD5;
#endif

vec4 v_color0 : COLOR1;
vec4 v_fog : COLOR2;
vec2 v_shadowData : COLOR3;
vec2 v_lightmapUV : TEXCOORD0;
vec2 v_texcoord0 : TEXCOORD1;
vec3 v_position : TEXCOORD2;
vec3 v_worldPos : TEXCOORD3;
vec3 v_viewVector : TEXCOORD4;
