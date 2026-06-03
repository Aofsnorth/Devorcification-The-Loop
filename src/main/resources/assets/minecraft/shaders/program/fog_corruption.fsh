// Fog corruption fragment shader
// Deep purple/black fog with subtle noise animation
// Near-player clear bubble for claustrophobia effect
#version 150

in vec2 texCoord;
in vec3 worldPos;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float FogDensity;
uniform float BubbleRadius;
uniform vec3 FogColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
    vec4 base = texture(DiffuseSampler, texCoord);

    float fog = clamp(FogDensity, 0.0, 1.0);
    float n = noise(texCoord * 0.05 + Time * 0.05);
    fog *= 0.85 + 0.15 * n;

    // Bubble clarity: linear falloff from center over BubbleRadius
    float distFromCenter = length(texCoord - 0.5) * 2.0;
    float bubble = smoothstep(0.0, BubbleRadius, distFromCenter);
    fog *= bubble;

    vec3 col = mix(base.rgb, FogColor, fog);
    fragColor = vec4(col, base.a);
}
