#version 150

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;
uniform float SmokeIntensity;

out vec2 texCoord;
out vec3 worldNormal;
out float smokeOffset;

float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

void main() {
    vec3 displaced = Position;
    float n = hash(Position * 4.0 + Time * 0.5);
    displaced += Normal * (n - 0.5) * 0.08 * SmokeIntensity;

    vec4 pos = ModelViewMat * vec4(displaced, 1.0);
    gl_Position = ProjMat * pos;
    texCoord = UV0;
    worldNormal = Normal;
    smokeOffset = n;
}
