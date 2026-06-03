// Devorcification corruption fragment shader
// Vignette + chromatic aberration + film grain + VHS tracking + color grade
#version 150

in vec2 texCoord;
in vec2 screenPos;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Time;
uniform float VignetteStrength;
uniform float ChromaticAmount;
uniform float GrainIntensity;
uniform float VhsIntensity;

// Inigo Quilez hash + simplex-like noise (no texture sampler)
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

vec3 sampleSplit(sampler2D tex, vec2 uv, float amount) {
    vec2 dir = uv - 0.5;
    float r = texture(tex, uv - dir * amount).r;
    float g = texture(tex, uv).g;
    float b = texture(tex, uv + dir * amount).b;
    return vec3(r, g, b);
}

void main() {
    vec2 uv = texCoord;

    // Chromatic aberration (stronger toward edges)
    float edgeDist = length(uv - 0.5) * 1.414;
    float ca = ChromaticAmount * edgeDist;
    vec3 col = sampleSplit(DiffuseSampler, uv, ca);

    // Color grade: desaturate 20%, shadow greenish, highlight cold blue, contrast 1.2
    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(luma);
    col = mix(col, gray, 0.20);
    vec3 shadowTint = vec3(0.10, 0.15, 0.10);
    vec3 highlightTint = vec3(0.80, 0.85, 1.00);
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    col *= mix(shadowTint, highlightTint, smoothstep(0.0, 1.0, lum));
    col = (col - 0.5) * 1.2 + 0.5;

    // Film grain
    float g = noise(uv * OutSize * 0.5 + Time * 60.0) - 0.5;
    col += vec3(g) * GrainIntensity;

    // Vignette with heartbeat pulse
    float r = length(uv - 0.5) * 1.414;
    float vig = smoothstep(0.85, 0.2, r);
    float pulse = 0.8 + 0.2 * sin(Time * 3.14159 * (60.0 / 60.0));
    float vigAmount = VignetteStrength * pulse;
    col *= mix(1.0 - vigAmount, 1.0, vig);

    // VHS tracking lines
    float line = sin((uv.y + Time * 0.05) * OutSize.y * 1.2);
    float glitch = step(0.985, hash(vec2(floor(uv.y * 80.0), floor(Time * 2.0))));
    float vhs = (line * 0.04 + glitch * 0.08) * VhsIntensity;
    col -= vec3(vhs);

    fragColor = vec4(col, 1.0);
}
